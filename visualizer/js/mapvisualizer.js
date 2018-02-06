(function(){
	var mapboxAccessToken = "pk.eyJ1IjoiZGFuaWVsZmIiLCJhIjoiY2ozZDR6eGU0MDA0ZzJxbnIyZzJ0YW1hcyJ9.7y9x4L3QsGkdls_1Qb5HKg";

	var agentNames = [];
	var agentTypes = {};

	var agentColours = {};
	var myMap = null;
	var geoJsonData = null;
	var ensemblesData = null;

	var timestampSteps = [];
	var currentTimestampIndex = 0;

	var playInterval = null;
	var playIntervalDuration = 1000;

	var mapLinks = [];
	var blockedLinks = [];
	var planLinks = [];

	var lastTimestampCollective = 0;
	var lastTimestampSelfish = 0;
	var lastCollectiveRead = false;

	var readPlanInterval = null;
	var readPlanIntervalDuration = 5000;

	var agentLocations = {};
	var initialLocations = {};
	var agentTimestamps = {};

	var osmLabelToCoordinates = {};

	var currentAdaptationId = 1;

	var lastZoomLevel = null;
	var lastZoomCenter = null;

	var distanceChart = null;
	var distanceChartSelfish = null;

	var planUrlCollective = "http://localhost:5000/get_cooperative_plan";
	var planUrlSelfish = "http://localhost:5000/get_selfish_plan";

	function readPlan(){
		readCollectivePlan();
		if (lastCollectiveRead)
			readSelfishPlan();
	}

	function readCollectivePlan() {
		$.getJSON(planUrlCollective, function(json){
			if (lastTimestampCollective != json["timestamp"]){
				geoJsonData = json["geojson"];
				ensemblesData = json["ensembles"];

				setCollectiveVariables();

				showMap();
				showUpdatedDistances();

				lastTimestampCollective = json["timestamp"];

				showNotification("Collective solution read", "success")

				lastCollectiveRead = true;
			}
		});
	}

	function readSelfishPlan() {
		$.getJSON(planUrlSelfish, function(json){
			if (lastTimestampSelfish != json["timestamp"]){
				showUpdatedSelfishDistances(json["geojson"]);

				lastTimestampSelfish = json["timestamp"];

				showNotification("Selfish solution read", "success")

				lastCollectiveRead = false;
			}
		});
	}

	function setCollectiveVariables() {
		cleanCollectiveVariables();

		setAgentNamesAndTypes();
		setAgentColourPairs();
		setStreetsFromGeoJSON();
		setTimestampsFromGeoJSON();
		setInitialPositionsFromGeoJSON();
		setOsmLabelCoordinatesCorrespondences();
		setAgentTimestampsFromGeoJSON();

		createCollectiveDistanceChart();
	}

	function createDistanceChart(domId, labels, data, colours){
		return new Chart($("#" + domId), {
			type: 'bar',
			data: {
				labels: labels,
				datasets: [{
					data: data,
					backgroundColor: colours
				}]
			},
			options: {
				legend: {
					display: false
				},
				scales: {
					yAxes: [{
						ticks: {
							beginAtZero:true
						}
					}]
				}
			}
		});
	}

	function createCollectiveDistanceChart(){
		var chartData = [], chartColours = [];

		for (var i = 0; i < agentNames.length; ++i){
			chartData.push(0);
			chartColours.push(agentColours[agentNames[i]]);
		}

		if (distanceChart != null)
			distanceChart.destroy();
		distanceChart = createDistanceChart("distance-chart", agentNames, chartData, chartColours);
	}

	function cleanCollectiveVariables(){
		agentNames = [];
		agentTypes = [];
		// agentColours = {}; keep same colours between executions
		initialLocations = {};
		agentLocations = {};
		agentTimestamps = {};
		timestampSteps = [];
		currentTimestampIndex = 0;
		clearInterval(playInterval);
		playInterval = null;
		mapLinks = [];
		blockedLinks = [];
		planLinks = [];
		$("#maplogs").text("Current Timestamp: 0.0");
	}

	function showPreviousMap(){
		if (currentTimestampIndex > 0) {
			currentTimestampIndex -= 1;
			showMap();
		}
	}

	function showNextMap(){
		if (currentTimestampIndex < timestampSteps.length - 1) {
			currentTimestampIndex += 1;

			showUpdatedDistances();

			if ($("#toggle-block-link-on-step").is(":checked")) {
				var randomInt = getRandomInt(0, 100);
				var selectedProbability = parseInt($("#toggling-probability").val());
				if (randomInt <= selectedProbability) {
					toggleBlockRandomPlannedLinks(1);
				}
				else {
					showMap();
				}
			}
			else {
				showMap();
			}
		}
		else {
			stopPlayInterval();
		}
	}

	function stopPlayInterval(){
		clearInterval(playInterval);
		playInterval = null;
		$("#play-timestamp-button .glyphicon").removeClass("glyphicon-pause").addClass("glyphicon-play");
		$("#play-timestamp-button .nav-btn-text").text("Play");
	}

	function getAgentDistancesFromGeoJson(geoJson, agentDistances, agentTypeDistances, filterOnlyLowerTimestamps) {
		for (var i = 0; i < agentNames.length; ++i) {
			agentDistances[agentNames[i]] = 0.0;
		}

		for (var i = 0; i < geoJson.length; ++i) {
			if (geoJson[i].geometry.type == "LineString") {
				var acceptEvent = true;
				if (filterOnlyLowerTimestamps && geoJson[i].properties.timestamp > timestampSteps[currentTimestampIndex]){
					acceptEvent = false;
				}
				if (acceptEvent) {
					var agentId = geoJson[i].properties.agent_id;
					if (agentId) {
						agentDistances[agentId] += geoJson[i].properties.distance;
					}
				}
			}
		}

		for (var agentType in agentTypes) {
			var agentSum = 0;
			var agentList = agentTypes[agentType];
			for (var i = 0; i < agentList.length; ++i) {
				agentSum += agentDistances[agentList[i]];
			}
			agentTypeDistances[agentType] = agentSum;
		}
	}

	function showUpdatedDistances(){
		var agentDistances = {}, agentTypeDistances = {};
		getAgentDistancesFromGeoJson(geoJsonData, agentDistances, agentTypeDistances, true);

		var distanceText = "";
		for (var agentType in agentTypeDistances) {
			distanceText += agentType + ": " + agentTypeDistances[agentType] + " m\n";
		}
		$("#agent-distances").text(distanceText.trim());

		distanceChart.data.datasets[0].data = [];
		for (var i = 0; i < agentNames.length; ++i){
			distanceChart.data.datasets[0].data.push(agentDistances[agentNames[i]]);
		}
		distanceChart.update();
	}

	function showUpdatedSelfishDistances(geoJsonSelfish) {
		var agentDistancesSelfish = {}, agentTypeDistancesSelfish = {};
		getAgentDistancesFromGeoJson(geoJsonSelfish, agentDistancesSelfish, agentTypeDistancesSelfish, false);

		var agentDistancesSelfishArray = [], agentColoursSelfish = [];
		for (var i = 0; i < agentNames.length; ++i){
			agentDistancesSelfishArray.push(agentDistancesSelfish[agentNames[i]]);
			agentColoursSelfish.push(agentColours[agentNames[i]]);
		}

		var totalDistance = 0;
		for (var agentType in agentTypeDistancesSelfish) {
			totalDistance += agentTypeDistancesSelfish[agentType];
		}
		$("#agent-distances-selfish").text("Total distance: " + totalDistance);

		if (distanceChartSelfish != null)
			distanceChartSelfish.destroy();
		distanceChartSelfish = createDistanceChart("distance-chart-selfish", agentNames, agentDistancesSelfishArray, agentColoursSelfish);
	}

	function setAgentNamesAndTypes(){
		for (var i = 0; i < geoJsonData.length; ++i){
			var agentType = geoJsonData[i].properties.agent_type;
			if (agentType != null && agentTypes[agentType] == null){
				agentTypes[agentType] = [];
			}
			var agentId = geoJsonData[i].properties.agent_id;
			if (agentId != null && agentNames.indexOf(agentId) < 0){
				agentNames.push(agentId);
				agentTypes[agentType].push(agentId);
			}
		}
	}

	function setAgentColourPairs(){
		for (var i = 0; i < agentNames.length; ++i){
			var agentId = agentNames[i];
			if (agentId != null && agentColours[agentId] == null)
				agentColours[agentId] = randomColor();
		}
	}

	function setStreetsFromGeoJSON(){
		for (var i = 0; i < geoJsonData.length; ++i) {
			if (geoJsonData[i].properties.original_link) {
				var startLabel = geoJsonData[i].properties.start_label;
				var destLabel = geoJsonData[i].properties.end_label;
				var coordinates = geoJsonData[i].geometry.coordinates;

				if (linkListContains(mapLinks, startLabel, destLabel) < 0) {
					var linkObj = {"start": startLabel, "end": destLabel, "start_coordinates": coordinates[0], "end_coordinates": coordinates[coordinates.length - 1]};
					mapLinks.unshift(linkObj);

					if (geoJsonData[i].properties.blocked) {
						blockedLinks.unshift(linkObj);
					}
				}
			}
			else if (geoJsonData[i].properties.link_in_plan) {
				var startLabel = geoJsonData[i].properties.start_label;
				var destLabel = geoJsonData[i].properties.end_label;
				var coordinates = geoJsonData[i].geometry.coordinates;

				if (linkListContains(planLinks, startLabel, destLabel) < 0) {
					var linkObj = {"start": startLabel, "end": destLabel, "start_coordinates": coordinates[0], "end_coordinates": coordinates[coordinates.length - 1]};
					planLinks.unshift(linkObj);
				}
			}
		}
	}

	function setTimestampsFromGeoJSON(){
		for (var i = 0; i < geoJsonData.length; ++i){
			var ts = geoJsonData[i].properties.timestamp;
			if (ts != null && timestampSteps.indexOf(ts) < 0){
				timestampSteps.push(ts);
			}
		}
		timestampSteps.sort(function(a, b){
			return a - b;
		});
	}

	function setInitialPositionsFromGeoJSON(){
		for (var i = 0; i < geoJsonData.length; ++i){
			var isInit = geoJsonData[i].properties.init_node;
			if (isInit){
				var agentId = geoJsonData[i].properties.agent_id;
				var nodeLabel = geoJsonData[i].properties.node_label;
				initialLocations[agentId] = nodeLabel;
			}
		}
		agentLocations = JSON.parse(JSON.stringify(initialLocations));
	}

	function setOsmLabelCoordinatesCorrespondences(){
		for (var i = 0; i < geoJsonData.length; ++i){
			if (geoJsonData[i].geometry.type == "Point") {
				var label = geoJsonData[i].properties.node_label;
				var coordinates = geoJsonData[i].geometry.coordinates;
				osmLabelToCoordinates[label] = coordinates;
			}
		}
	}

	function setAgentTimestampsFromGeoJSON(){
		for (var i = 0; i < geoJsonData.length; ++i){
			if (geoJsonData[i].geometry.type == "Point") {
				var agent = geoJsonData[i].properties.agent_id;
				var ts = geoJsonData[i].properties.timestamp;
				if (agent != null && ts != null){
					if (agentTimestamps[agent] == null){
						agentTimestamps[agent] = [];
					}
					agentTimestamps[agent].push(ts);
				}
			}
		}
		for (var agent in agentTimestamps){
			agentTimestamps[agent] = agentTimestamps[agent].sort(function(a, b){
				return a - b;
			});
		}
	}

	function getAverageCoordinates(){
		var latSum = 0, lonSum = 0;
		var accountedItems = 0;
		for (var i = 0; i < geoJsonData.length; ++i){
			var geometryItem = geoJsonData[i].geometry;
			if (geometryItem != null && geometryItem.type == "Point" && geometryItem.coordinates != null) {
				latSum += geometryItem.coordinates[1];
				lonSum += geometryItem.coordinates[0];
				++accountedItems;
			}
		}

		var result = [];
		result.push(latSum / accountedItems);
		result.push(lonSum / accountedItems);
		return result;
	}

	function addLegendToMap(){
		var legend = L.control({position: 'bottomright'});
		legend.onAdd = function(map) {
			var div = L.DomUtil.create('div', 'info legend');
			var labels = ["<strong>Legend</strong>"];

			for (var agentId in agentColours){
				var col = agentColours[agentId];
				var infoText = "";
				if (ensemblesData != null && ensemblesData[agentId] != null && ensemblesData[agentId].length > 0) {
					infoText = "[";
					for (var i = 0; i < ensemblesData[agentId].length; ++i) {
						if (i > 0){
							infoText += ", ";
						}
						infoText += ensemblesData[agentId][i];
					}
					infoText += "]";
				}
				labels.push(
					"<i class='legend-item' data-agent='" + agentId  + "'style='background:" + col + ";'><b>" + agentId + "</b> " + infoText + "</i>");
			}

			div.innerHTML = labels.join('<br>');
			return div;
		};
		legend.addTo(myMap);
	}

	function createMap() {
		if (myMap != null) {
			myMap.off();
			myMap.remove();
		}

		if (lastZoomLevel == null || lastZoomCenter == null) {
			var avgCoords = getAverageCoordinates(geoJsonData);
			myMap = L.map("mapid").setView([avgCoords[0], avgCoords[1]], 15);
		}
		else {
			myMap = L.map("mapid").setView(lastZoomCenter, lastZoomLevel);
		}

		L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=' + mapboxAccessToken, {
									maxZoom: 25,
									attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
															 '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
															 'Imagery © <a href="http://mapbox.com">Mapbox</a>',
									id: 'mapbox.streets'
								}).addTo(myMap);
		L.MakiMarkers.accessToken = mapboxAccessToken;
		L.MakiMarkers.smallOptions.iconSize = [15, 40];
		addLegendToMap(myMap);
		myMap.on("zoomend, moveend", onMapPositionChanged);
	}

	function onMapPositionChanged() {
		lastZoomLevel = myMap.getZoom();
		lastZoomCenter = myMap.getCenter();
	}

	function addStyle(feature) {
		var agentId = feature.properties.agent_id;
		if (agentId != null)
			return {color: agentColours[agentId], opacity: 0.7};
		if (feature.properties.original_link) {
			if (linkListContains(blockedLinks, feature.properties.start_label, feature.properties.end_label) >= 0)
				return {color: "#ff0000", opacity: 1.0, weight: 2.0};
			else
				return {color: "#000000", opacity: 0.05};
		}
	}

	function pointToLayer(feature, latlng) {
		var agentId = feature.properties.agent_id;
		var timestamp = feature.properties.timestamp;
		var iconName = "";
		if (feature.properties.init_node){
			iconName = "village";
		}
		else if (feature.properties.end_node){
			iconName = "embassy";
		}
		else if (feature.properties.agent_type == "pedestrian"){
			iconName = "pitch";
		}
		else if (feature.properties.agent_type == "vehicle"){
			iconName = "car";
		}
		var icon = L.MakiMarkers.icon({icon: iconName, color: agentColours[agentId], size: "m", className: agentId +"-marker"});
		return L.marker(latlng, {icon: icon});
	}

	function onEachFeature(feature, layer) {
		if (feature.properties.timestamp == timestampSteps[currentTimestampIndex] && feature.geometry.type == "Point") {
				var eventContent = feature.properties.event_content;
//				if (eventContent != null && eventContent.length > 0) {
//					var popup = L.popup();
//					popup.setLatLng([feature.geometry.coordinates[1], feature.geometry.coordinates[0]]);
//					popup.setContent(feature.properties.event_content);
//					popup.addTo(myMap);
//				}

				if (feature.properties.agent_id != null) {
					agentLocations[feature.properties.agent_id] = feature.properties.node_label;
				}

				var currentText = $("#maplogs").text();
				$("#maplogs").text(currentText + '\n' + eventContent);
		}

		if (feature.properties && feature.properties.event_content) {
			var eventContent = feature.properties.event_content;
			if (feature.properties.timestamp) {
				eventContent += " (" + feature.properties.timestamp + ")";
			}
			layer.bindPopup(eventContent);
		}

		layer.on({
			click: function(){
				if (feature.properties.original_link) {
					toggleBlockLink(feature.properties.start_label, feature.properties.end_label, feature.geometry.coordinates);
					showMap();
				}
			}
		});
	}

	function linkListContains(linkList, startLabel, endLabel){
		for (var i = 0; i < linkList.length; ++i) {
			if ((linkList[i]["start"] == startLabel && linkList[i]["end"] == endLabel) ||
					(linkList[i]["end"] == startLabel && linkList[i]["start"] == endLabel)) {
						return i;
			}
		}
		return -1;
	}

	function getClosestAgentTimestampToTimestamp(agentId, refTs) {
		var agentTs = agentTimestamps[agentId];
		if (agentTs != null && agentTs.length > 0) {
			if (agentTs.length > 1) {
				for (var i = 0; i < agentTs.length - 1; ++i) {
					if (agentTs[i] <= refTs && refTs < agentTs[i + 1]){
						return agentTs[i];
					}
				}
			}
			else if (agentTs[0] <= refTs){
				return agentTs[0];
			}
		}
		return -1;
	}

	function applyFilter(feature, layer) {
		if (feature.properties.hide) {
			return false;
		}

		if (feature.properties.original_link || feature.properties.init_node || feature.properties.end_node) {
			return true;
		}

		if (feature.geometry.type == "LineString") {
			return feature.properties.timestamp <= timestampSteps[currentTimestampIndex];
		}
		else if (feature.geometry.type == "Point") {
			var agentId = feature.properties.agent_id;
			if (agentId != null) {
				var closestTs = getClosestAgentTimestampToTimestamp(agentId, timestampSteps[currentTimestampIndex]);
				return feature.properties.timestamp == closestTs;
			}
		}

		return false;

	}

	function getAllBlockedLinks() {
		var linksJson = [];
		for (var i = 0; i < blockedLinks.length; ++i) {
			var lk = blockedLinks[i];
			var nearestAgent = getNearestAgentToLink(lk);
			linksJson.push({"init_pos": lk["start"], "target_pos": lk["end"], "nearest_agent": nearestAgent});
			linksJson.push({"init_pos": lk["end"], "target_pos": lk["start"], "nearest_agent": nearestAgent});
		}
		return linksJson;
	}

	function getNearestAgentToLink(link) {
		var minDistance = Number.MAX_SAFE_INTEGER;
		var minAgent = null;
		for (var agent in agentLocations) {
			var location = osmLabelToCoordinates[agentLocations[agent]];
			if (location != null) {
				var d1 = distance(link["start_coordinates"][1], link["start_coordinates"][0], location[1], location[0]);
				var d2 = distance(link["end_coordinates"][1], link["end_coordinates"][0], location[1], location[0]);
				var mind = Math.min(d1, d2);
				if (mind < minDistance){
					minDistance = mind;
					minAgent = agent;
				}
			}
		}
		return minAgent;
	}

	// source: http://www.geodatasource.com/developers/javascript
	function distance(lat1, lon1, lat2, lon2, unit) {
		var radlat1 = Math.PI * lat1/180
		var radlat2 = Math.PI * lat2/180
		var theta = lon1-lon2
		var radtheta = Math.PI * theta/180
		var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
		dist = Math.acos(dist)
		dist = dist * 180/Math.PI
		dist = dist * 60 * 1.1515
		if (unit=="K") { dist = dist * 1.609344 }
		if (unit=="N") { dist = dist * 0.8684 }
		return dist
	}

	function showMap() {
		$("#maplogs").text("Timestamp: " + timestampSteps[currentTimestampIndex]);
		createMap();
		L.geoJSON(geoJsonData, {
				style: addStyle,
				pointToLayer: pointToLayer,
				onEachFeature: onEachFeature,
				filter: applyFilter
		}).addTo(myMap);
	}

	function toggleBlockLink(startLabel, endLabel, coordinates) {
		var pos = linkListContains(blockedLinks, startLabel, endLabel);
		if (pos >= 0) {
			blockedLinks.splice(pos, 1);
		}
		else {
			blockedLinks.unshift({"start": startLabel,
														"end": endLabel,
														"start_coordinates": coordinates[0],
														"end_coordinates": coordinates[coordinates.length - 1]
													});
		}
	}

	function getRandomInt(min, max) {
		return Math.floor(Math.random() * (max - min + 1)) + min;
	}

	function toggleBlockRandomLink(){
		var randomLink = mapLinks[getRandomInt(0, mapLinks.length)];
		toggleBlockLink(randomLink["start"], randomLink["end"], [randomLink["start_coordinates"], randomLink["end_coordinates"]]);
	}

	function toggleBlockRandomLinks(numBlocked){
		for (var i = 0; i < numBlocked; ++i) {
			toggleBlockRandomLink();
		}

		if (numBlocked > 0) {
			showMap();
		}
	}

	function toggleBlockRandomPlannedLinks(numBlocked){
		for (var i = 0; i < numBlocked; ++i) {
			var randPlanLink = planLinks[getRandomInt(0, planLinks.length - 1)];
			toggleBlockLink(randPlanLink["start"], randPlanLink["end"], [randPlanLink["start_coordinates"], randPlanLink["end_coordinates"]]);
		}

		if (numBlocked > 0) {
			showMap();
		}
	}

	function highlightAgentAndEnsembles(agentId, doHighlight){
		var agentsToHighlight = [agentId];
		if (ensemblesData != null && ensemblesData[agentId] != null) {
			for (var i = 0; i < ensemblesData[agentId].length; ++i) {
				agentsToHighlight.push(ensemblesData[agentId][i]);
			}
		}

		for (var i = 0; i < agentsToHighlight.length; ++i) {
			var nodes = $("." + agentsToHighlight[i] + "-marker");
			if (doHighlight) {
				nodes.addClass("highlighted-marker");
			}
			else {
				nodes.removeClass("highlighted-marker");
			}
		}
	}

	function showNotification(message, type) {
		$.notify({
			message: message
		},{
			type: type,
			delay: 3000
		});
	}

//	function getPlanPortListenerFromUrl(){
//		var urlParams = window.location.search.substr(1);
//		var urlParamsSplit = urlParams.split("&");
//		for (var i = 0; i < urlParamsSplit.length; ++i) {
//			var keyValue = urlParamsSplit[i].split("=");
//			if (keyValue.length == 2) {
//				if (keyValue[0] == "port") {
//					return parseInt(keyValue[1]);
//				}
//			}
//		}
//		return 5000;
//	}

	$(document).ready(function(){
		readPlanInterval = setInterval(readPlan, readPlanIntervalDuration);
	});

	$(document).on("click", "#back-timestamp-button", function(){
		stopPlayInterval();
		showPreviousMap();
	});

	$(document).on("click", "#next-timestamp-button", function(){
		stopPlayInterval();
		showNextMap();
	});

	$(document).on("click", "#play-timestamp-button", function(){
		if (playInterval == null) {
			playInterval = setInterval(showNextMap, playIntervalDuration);
			$("#play-timestamp-button .glyphicon").removeClass("glyphicon-play").addClass("glyphicon-pause");
			$("#play-timestamp-button .nav-btn-text").text("Pause");
		}
		else {
			stopPlayInterval();
		}
	});

	$(document).on("click", "#restart-timestamp-button", function(){
		stopPlayInterval();
		currentTimestampIndex = 0;
		agentLocations = JSON.parse(JSON.stringify(initialLocations));
		showMap();
		showUpdatedDistances();
	});

    $(document).on("click", "#open-scenario-config-button", function(){
        $("#random-scenario-modal").modal("show");
    });

    $(document).on("click", "#create-random-scenario-button", function(){
        showNotification("Generating a new scenario...", "info");

        var sendObj = {"passengers": parseInt($("#num-passengers").val()),
                       "carpools": parseInt($("#num-carpools").val()),
                       "min_latitude": parseFloat($("#min-latitude").val()),
                       "max_latitude": parseFloat($("#max-latitude").val()),
                       "min_longitude": parseFloat($("#min-longitude").val()),
                       "max_longitude": parseFloat($("#max-longitude").val()),
                       "min_walk_range": parseInt($("#min-walk-range").val()),
                       "max_walk_range": parseInt($("#max-walk-range").val())
                      };

        $("#random-scenario-modal").modal("hide");
        stopPlayInterval();

        var targetUrl = "http://localhost:5000/generate_scenario";
        $.ajax({
            "url": targetUrl,
            "method": "POST",
            "headers": {
                "content-type": "application/json"
            },
            "data": JSON.stringify(sendObj)
        })
        .fail(function(){
            showNotification("Could not generate a new scenario", "danger");
        })
        .done(function(response){
            showNotification("Scenario successfully generated!", "success");
            // $("#open-scenario-config-button").prop("disabled", true);  // block button on success
        });
    });

	$(document).on("click", "#send-current-state-button", function(){
		showNotification("Sending current state to get a new solution", "info");

		stopPlayInterval();
		var targetUrl = "http://localhost:5000/run_adaptation";
		var retJSON = {
                        "adaptations": {
                            "agents": agentLocations,
                            "blocked_streets": getAllBlockedLinks(),
                            "id": currentAdaptationId
                        }
                      };

		$.ajax({
			"url": targetUrl,
			"method": "POST",
			"headers": {
				"content-type": "application/json"
			},
			"data": JSON.stringify(retJSON)
		})
        .fail(function(){
            showNotification("The current state could not be sent", "danger");
        })
        .done(function(response){
            showNotification("A new solution has been obtained!", "success");
			++currentAdaptationId;
		});
	});

	$(document).on("click", "#toggle-block-random-link-button", function(){
		var numBlocked = parseInt($("#num-block-random-link").val());
		toggleBlockRandomLinks(numBlocked);
	});

	$(document).on("click", "#toggle-block-planned-link-button", function(){
		var numBlocked = parseInt($("#num-block-planned-link").val());
		toggleBlockRandomPlannedLinks(numBlocked);
	});

	$(document).on("mouseover", ".legend-item", function(e){
		var agentId = $(this).attr("data-agent");
		highlightAgentAndEnsembles(agentId, true);
	});

	$(document).on("mouseout", ".legend-item", function(e){
		var agentId = $(this).attr("data-agent");
		highlightAgentAndEnsembles(agentId, false);
	});
}());
