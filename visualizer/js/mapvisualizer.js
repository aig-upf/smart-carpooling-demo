(function(){
	var mapboxAccessToken = "pk.eyJ1IjoiZGFuaWVsZmIiLCJhIjoiY2ozZDR6eGU0MDA0ZzJxbnIyZzJ0YW1hcyJ9.7y9x4L3QsGkdls_1Qb5HKg";

	var agentNames = [];
	var agentTypes = {};

	var agentColours = {};
	var myMap = null;
	var geoJsonData = null;

	var timestampSteps = [];
	var currentTimestampIndex = 0;

	var playInterval = null;
	var playIntervalDuration = 1000;

	var mapLinks = [];
	var blockedLinks = [];
	var planLinks = [];

	var lastTimestamp = 0;
	var readPlanInterval = null;
	var readPlanIntervalDuration = 10000;

	var agentLocations = {};
	var initialLocations = {};
	var agentTimestamps = {};

	var osmLabelToCoordinates = {};

	var currentAdaptationId = 1;

	var lastZoomLevel = null;
	var lastZoomCenter = null;

	var distanceChart = null;

	var planUrl = "";

	var shutdownProviderOnPlanReception = true;

	function readPlan(){
		$.getJSON(planUrl, function(json){
			if (lastTimestamp != json["timestamp"]){
				resetVariables();
				geoJsonData = json["geojson"];
				setAgentNamesAndTypes();
				setAgentColourPairs();
				getStreetsFromGeoJSON();
				getTimestampsFromGeoJSON();
				getInitialPositionsFromGeoJSON();
				getOsmLabelCoordinatesCorrespondences();
				getAgentTimestampsFromGeoJSON();
				createDistanceChart();
				showMap();
				showUpdatedDistances();
				lastTimestamp = json["timestamp"];

				if (shutdownProviderOnPlanReception) {
					shutdownPlanProvider();
				}
			}
		});
	}

	function shutdownPlanProvider(){
		$.post(planUrl + "/shutdown");
	}

	function createDistanceChart(){
		var chartData = [], chartColours = [];

		for (var i = 0; i < agentNames.length; ++i){
			chartData.push(0);
			chartColours.push(agentColours[agentNames[i]]);
		}

		distanceChart = new Chart($("#distance-chart"), {
			type: 'bar',
			data: {
				labels: agentNames,
				datasets: [{
					data: chartData,
					backgroundColor: chartColours
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

	function resetVariables(){
		agentNames = [];
		agentTypes = [];
		agentColours = {};
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

	function showUpdatedDistances(){
		var currentTimestamp = timestampSteps[currentTimestampIndex];
		var agentDistances = {};

		for (var i = 0; i < agentNames.length; ++i) {
			agentDistances[agentNames[i]] = 0.0;
		}

		for (var i = 0; i < geoJsonData.length; ++i) {
			if (geoJsonData[i].properties.timestamp <= currentTimestamp && geoJsonData[i].geometry.type == "LineString") {
				var agentId = geoJsonData[i].properties.agent_id;
				agentDistances[agentId] += geoJsonData[i].properties.distance;
			}
		}

		var agentTypeDistances = {};

		for (var agentType in agentTypes) {
			var agentSum = 0;
			var agentList = agentTypes[agentType];
			for (var i = 0; i < agentList.length; ++i) {
				agentSum += agentDistances[agentList[i]];
			}
			agentTypeDistances[agentType] = agentSum;
		}

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

	function getStreetsFromGeoJSON(){
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

	function getTimestampsFromGeoJSON(){
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

	function getInitialPositionsFromGeoJSON(){
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

	function getOsmLabelCoordinatesCorrespondences(){
		for (var i = 0; i < geoJsonData.length; ++i){
			if (geoJsonData[i].geometry.type == "Point") {
				var label = geoJsonData[i].properties.node_label;
				var coordinates = geoJsonData[i].geometry.coordinates;
				osmLabelToCoordinates[label] = coordinates;
			}
		}
	}

	function getAgentTimestampsFromGeoJSON(){
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
				labels.push(
					"<i class='legend-item' style='background:" + col + ";'>" + agentId + "</i>");
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
		var icon = L.MakiMarkers.icon({icon: iconName, color: agentColours[agentId], size: "m"});
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

	function getPlanPortListenerFromUrl(){
		var urlParams = window.location.search.substr(1);
		var urlParamsSplit = urlParams.split("&");
		for (var i = 0; i < urlParamsSplit.length; ++i) {
			var keyValue = urlParamsSplit[i].split("=");
			if (keyValue.length == 2) {
				if (keyValue[0] == "port") {
					return parseInt(keyValue[1]);
				}
			}
		}
		return 5000;
	}

	$(document).ready(function(){
		planUrl = "http://localhost:" + getPlanPortListenerFromUrl();
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

	$(document).on("click", "#send-current-state-button", function(){
		stopPlayInterval();
		var targetUrl = "http://178.239.178.239:8080/sendAdaptation.php";
		var retJSON = {
										'adaptations': {
											'agents': agentLocations,
											'blocked_streets': getAllBlockedLinks(),
											'id': currentAdaptationId
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
		.done(function(response){
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
}());
