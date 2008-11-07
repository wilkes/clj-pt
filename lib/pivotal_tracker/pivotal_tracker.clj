(ns pivotal-tracker
  (:require (clojure.contrib (except :as except))
	    (pivotal-tracker (xml :as xml)
			     (webclient :as client))))


;; ssl off be default
(def *base-url*)
(defn use-ssl []
  (def *base-url* "https://www.pivotaltracker.com/services/v1/projects/"))
(defn no-ssl []
  (def *base-url* "http://www.pivotaltracker.com/services/v1/projects/"))
(no-ssl)

(defn project-url [project-id]
  (str *base-url* project-id))

(defn story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn fetch-item [token url]
  (xml/to-struct (-> (client/fetch-as-xml token url) :content first)))

(defn fetch-collection [token url]
  (reduce #(cons (xml/to-struct %2) %1) 
	  [] (-> (client/fetch-as-xml token url) :content second :content)))

(defn valid-story? [s]
  (let [required #{:name :requested_by}
	optional #{:id :story_type :url :estimate :current_state :description :created_at}
	all-fields (concat required optional)]
    (and
     (every? #(contains? s %) required)
     (every? #(.contains all-fields %) (keys s)))))


(defn get-project [token project-id]
  (fetch-item token (project-url project-id)))

(defn get-story [token project-id story-id]
  (fetch-item token (story-url project-id story-id)))

(defn get-all-stories [token project-id]
  (fetch-collection token (story-url project-id)))

;; Currently the client is responsbile for encoding 
(defn search-stories [token project-id criteria]
  (fetch-collection token (str (story-url project-id) "?filter=" criteria)))

;; These are all returning HttpRespons objects for now
(defn add-story [token project-id story]
  (except/throw-if (not (valid-story? story)) "Invalid story")
  (client/post token
	    (story-url project-id)
	    (xml/to-xml :story story)))

;; Always getting a 500?!?!
(defn update-story [token project-id story-id update]
  (client/post token
	       (story-url project-id story-id)
	       :story update))

(defn delete-story [token project-id story-id]
  (client/delete token (story-url project-id story-id)))

(defn deliver-finished-stories [token project-id]
  (client/put token 
	      (str (project-url project-id) "/stories_deliver_all_finished")))

