Clojure library for pivotal tracker

http://www.pivotaltracker.com/help/api

Sample Usage:

       ; (use-ssl) or (no-ssl)
       ; (use-ssl) is on by default
       (use 'pivotal-tracker)
       (def token "--token--")
       (def project-id 1234)
       (def my-project (context token project-id))
       
       ;; list all the stories in my-project
       (my-project all)
       
       ;; what has my boss requested that hasn't been started
       (my-project all (requester "Pointed Haired Boss") unstarted)

       (my-project all unstarted bug)
       
       ;; what's ready for QA?
       (my-project all delivered)

       ;;how big is that
       (points (my-project all delivered))
       
       ;; give me all the story for the chores that people
       (map :id (my-project all))
       ;;
       (my-project collect :id)

       ;; create a story
       (my-project add {:name "My Story" :requested_by "Wilkes Joiner"
                        :description "This is a story of a man named Jed"})

	
	;; lookup my story and update, then throw it away
	(let [id (:id (first (my-project all (:requester "Wilkes Joiner") 
		    		    (exact "a man named Jed"))))]
	  (my-project update id {:description "Black gold that is, Texas Tea."})
	  (my-project delete id))
	
	;; get all the ids, names and descriptions for the unstarted bugs
	(my-project collect (:id :name :description) unstarted bug)