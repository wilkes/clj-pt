(ns pivotal-tracker
  (:require (clojure.contrib (except :as except))
	    (pivotal-tracker (xml :as xml)
			     (webclient :as client)
			     (query :as query))))

;; ssl on be default
(declare *base-url*)
(defn use-ssl []
  (def *base-url* "https://www.pivotaltracker.com/services/v1/projects/"))
(defn no-ssl []
  (def *base-url* "http://www.pivotaltracker.com/services/v1/projects/"))
(use-ssl)

(defn- project-url [project-id]
  (str *base-url* project-id))

(defn- story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn- simple-xml [response]
  (-> response client/to-string xml/simple-parse))

(defn- fetch [token url]
  (simple-xml (client/get token url)))

(defn- valid-story? [s]
  (let [required #{:name :requested_by}
	optional #{:id :story_type :url :estimate :current_state 
		   :description :created_at}
	all-fields (concat required optional)]
    (and
     (every? #(contains? s %) required)
     (every? #(.contains all-fields %) (keys s)))))

;; Public API
(query/criteria label requester owner mywork id)

(query/enums type
	     feature bug chore release)

(query/enums state
	     unstarted started finished 
	     delivered accepted rejected)

(defn project [token project-id]
  "Returns a function works like apply but passing in token and project-id
  as paramaters"
  (fn [target & args]
    (apply target token project-id args)))

(defn info [token project-id]
  "Returns a map with the current project settings"
  (-> (fetch token (project-url project-id))
      :response :project))

(defn all 
  "Return a lazy seqs of story maps given an optional list of filters"
  [token project-id & filter]
  (let [url (str (story-url project-id) 
		 (if filter 
		   (str "?filter=" (apply query/combine filter))))]
    (map :story 
	 (-> (fetch token url) :response :stories))))

(defn add
  "Takes a story map and adds it to the project.  
  Returns the fully populated story.
  Story map requires a :name and :requested_by."
  [token project-id story]
  (let [url (story-url project-id)
	data (xml/to-xml :story story)]
    (except/throw-if (not (valid-story? story)) "Invalid story")
    (-> (simple-xml (client/post token url data))
	:response :story)))

(comment 
  "Always getting a 500?!?!"
  (defn update [token project-id story-id update]
    (let [url (story-url project-id story-id)
	  data (xml/to-xml :story update)]
      (simple-xml (client/post token url data)))))


(defn delete [token project-id story-id]
  "Deletes the story with story-id. Returns the response message string"
  (let [url (story-url project-id story-id)
	response (simple-xml (client/delete token url))]
    (-> response :response :message)))

(defn deliver-finished-stories [token project-id]
  "Mark all finished stories in the project as delivered."
  (let [url (str (project-url project-id) 
		    "/stories_deliver_all_finished")]
    (simple-xml (client/put token url))))


(defn points [stories]
  "Returns the sum the estimates of the story"
  (let [f (fn [result story]
	    (let [estimate (Integer/decode (story :estimate))]
	      (if (> estimate 0)
		(+ result estimate)
		result)))]
    (reduce f 0 stories)))

(defn reduce-stories [stories & keyword]
  "Returns a seq of seqs of the values of the keys for the given stories"
  (seq 
   (reduce #(conj %1 (map %2 keyword)) [] stories)))

