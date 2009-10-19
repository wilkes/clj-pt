Clojure library for pivotal tracker

http://www.pivotaltracker.com/help/api


Download the dependencies from the Downloads tab on the github project
page:

http://github.com/wilkes/pivotal-tracker-clojure-client/downloads


Sample Usage:

       ; (use-ssl) or (no-ssl)
       ; (use-ssl) is on by default
       (use 'pivotal-tracker)
       (def token "--token--")
       (def project-id 1234)
       (def my-project (project token project-id))
       
       ;; fetch a list of all the stories as maps
       (my-project all)
       
       ;; what has my boss requested that hasn't been started?
       (my-project all (requester "Pointed Haired Boss") unstarted)

       ;; all unstarted bugs
       (my-project all unstarted bug)
       
       ;; what's ready for QA?
       (my-project all delivered)

       ;; how big is that?
       (points (my-project all delivered))

       ;; what are the names of those stories
       (reduce-stories (my-project all delivered) :name)
       ;; returns (("name1") ("name2")...) 
       
       ;; create a story
       (my-project add {:name "My Story" :requested_by "Wilkes Joiner"
                        :description "This is a story of a man named Jed"})

	
       ;; lookup my story, then throw it away
       (let [id (:id (first (my-project all (:requester "Wilkes Joiner") 
       		    		    (exact "a man named Jed"))))]
         (my-project delete id))
	
       ;; get all the ids, names and descriptions for the unstarted bugs
       (reduce-stories (my-project all unstarted bug) (:id :name :description))
       ;; returns (("123" "name1", "description1") ...)
