(define (problem p02)
(:domain journey-planner)
(:objects
	c1 - carpool
	p1 p2 - pedestrian
	loc1 loc2 loc3 loc4 loc5 - location
)
(:init
	(at c1 loc1)
	(at p1 loc2)
	(at p2 loc3)

	(has-footpath p1 loc2 loc3)
	(has-footpath p1 loc3 loc2)

	(has-footpath p2 loc2 loc3)
	(has-footpath p2 loc3 loc2)

	(has-street loc1 loc2)
	(has-street loc2 loc1)

	(has-street loc2 loc5)
	(has-street loc5 loc2)

	(has-street loc4 loc5)
	(has-street loc5 loc4)

	(has-street loc1 loc4)
	(has-street loc4 loc1)

	(= (distance loc1 loc2) 4)
	(= (distance loc2 loc1) 4)

	(= (distance loc2 loc3) 1)
	(= (distance loc3 loc2) 1)

	(= (distance loc2 loc5) 4)
	(= (distance loc5 loc2) 4)

	(= (distance loc4 loc5) 4)
	(= (distance loc5 loc4) 4)

	(= (distance loc1 loc4) 4)
	(= (distance loc4 loc1) 4)

	(= (velocity p1 loc2 loc3) 1)
	(= (velocity p1 loc3 loc2) 1)

	(= (velocity p2 loc2 loc3) 1)
	(= (velocity p2 loc3 loc2) 1)

	(= (velocity c1 loc1 loc2) 2)
	(= (velocity c1 loc2 loc1) 2)

	(= (velocity c1 loc2 loc5) 2)
	(= (velocity c1 loc5 loc2) 2)

	(= (velocity c1 loc4 loc5) 2)
	(= (velocity c1 loc5 loc4) 2)

	(= (velocity c1 loc1 loc4) 2)
	(= (velocity c1 loc4 loc1) 2)
)
(:goal (and
	(at c1 loc4)
	(at p1 loc5)
	(at p2 loc5)
))
)
