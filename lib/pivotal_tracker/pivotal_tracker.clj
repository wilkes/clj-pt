(ns pivotal-tracker
  (:require (clojure.contrib (except :as except))
	    (pivotal-tracker (xml :as xml)
			     (webclient :as client)
			     (query :as query))))


;; ssl on be default
(def *base-url*)
(defn use-ssl []
  (def *base-url* "https://www.pivotaltracker.com/services/v1/projects/"))
(defn no-ssl []
  (def *base-url* "http://www.pivotaltracker.com/services/v1/projects/"))
(use-ssl)

(defn project-url [project-id]
  (str *base-url* project-id))

(defn story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn simple-xml [response]
  (-> response client/to-xml xml/simplify))

(defn fetch [token url]
  (xml/simplify (client/get-xml token url)))

(defn valid-story? [s]
  (let [required #{:name :requested_by}
	optional #{:id :story_type :url :estimate :current_state 
		   :description :created_at}
	all-fields (concat required optional)]
    (and
     (every? #(contains? s %) required)
     (every? #(.contains all-fields %) (keys s)))))



(defn context [token project-id]
  (fn [target & args]
    (apply target token project-id args)))



(query/criteria label requester owner mywork id)

(query/enums type
	     feature bug chore release)

(query/enums state
	     unstarted started finished 
	     delivered accepted rejected)

(defn info [token project-id]
  (-> (fetch token (project-url project-id))
      :response :project))

(defn all 
  ([token project-id & filter]
     (let [filter-str (if filter (str "?filter=" (apply query/combine filter)) "")
	   stories (fetch token (str (story-url project-id) filter-str))]
       (map :story (-> stories :response :stories)))))


(defn add [token project-id story]
  (except/throw-if (not (valid-story? story)) "Invalid story")
  (-> (simple-xml 
       (client/post token
		    (story-url project-id)
		    (xml/to-xml :story story)))
      :response :story))

(comment 
  "Always getting a 500?!?!"
  (defn update [token project-id story-id update]
    (simple-xml
     (client/post token
		  (story-url project-id story-id)
		  (xml/to-xml :story update)))))

(defn delete [token project-id story-id]
  (-> (simple-xml 
       (client/delete token (story-url project-id story-id)))
      :response :message))

(defn deliver-finished-stories [token project-id]
  (simple-xml 
   (client/put token 
	       (str (project-url project-id) 
		    "/stories_deliver_all_finished"))))

(defn points [stories]
  (let [f (fn [result story]
	    (let [estimate (Integer/decode (story :estimate))]
	      (if (> estimate 0)
		(+ result estimate)
		result)))]
    (reduce f 0 stories)))

(defn collect [token project-id keyword & criteria]
  (let [stories (apply all token project-id criteria)]
    (if (keyword? keyword)
      (map #(% keyword) stories)
      (map #(map % (seq keyword)) stories))))
