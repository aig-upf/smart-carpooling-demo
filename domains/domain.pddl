(define (domain journey-planner)
(:requirements :typing :durative-actions)
(:types
	bus taxi car bike carpool flexibus - vehicle
	pedestrian vehicle - agent
	agent location
)
(:predicates
	(at ?a - agent ?x - location)
	(in ?p - pedestrian ?v - vehicle)
	(has-footpath ?p - pedestrian ?x - location ?y - location)
	(has-street ?x - location ?y - location)
)
(:functions
	(velocity ?a - agent ?x - location ?y - location) - number
	(distance ?x - location ?y - location) - number
)
(:durative-action embark
	:parameters (?p - pedestrian ?v - vehicle ?x - location)
	:duration (= ?duration 1)
	:condition (and
					(at start (at ?p ?x))
					(over all (at ?v ?x))
				)
	:effect	(and
					(at end (in ?p ?v))
					(at start (not (at ?p ?x)))
				)
)
(:durative-action debark
	:parameters (?p - pedestrian ?v - vehicle ?x - location)
	:duration (= ?duration 1)
	:condition (and
					(at start (in ?p ?v))
					(over all (at ?v ?x))
				)
	:effect	(and
					(at end (at ?p ?x))
					(at start (not (in ?p ?v)))
				)
)
(:durative-action walk
	:parameters (?p - pedestrian ?x - location ?y - location)
	:duration (= ?duration (/ (distance ?x ?y) (velocity ?p ?x ?y)))
	:condition (and
					(at start (at ?p ?x))
					(at start (has-footpath ?p ?x ?y))
				)
	:effect	(and
					(at end (at ?p ?y))
					(at start (not (at ?p ?x)))
				)
)
(:durative-action travel
	:parameters (?v - vehicle ?x - location ?y - location)
	:duration (= ?duration (/ (distance ?x ?y) (velocity ?v ?x ?y)))
	:condition (and
					(at start (at ?v ?x))
					(at start (has-street ?x ?y))
				)
	:effect	(and
					(at end (at ?v ?y))
					(at start (not (at ?v ?x)))
				)
)
)
