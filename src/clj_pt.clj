(ns clj-pt
  (:require (clj-pt (xml :as xml)
                    (webclient :as web)
                    (query :as query))))

;; ssl on be default
(declare *base-url*)
(defn use-ssl []
  (def *base-url* "https://www.pivotaltracker.com/services/v2/"))
(defn no-ssl []
  (def *base-url* "http://www.pivotaltracker.com/services/v2/"))
(use-ssl)

(defn- project-url [project-id]
  (str *base-url* "projects/"  project-id))

(defn- story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn- validate-response [response]
  (let [r (xml/simple-parse response)]
    (prn r)
    (if (and (string? (r :response))
	     (= "500 error" (r :response))) 
      (throw (Exception.)))
    (if (-> r :response first :errors)
      (let [msgs (map :error (-> r :response first :errors))
	    errors (reduce #(str %1 ", and " %2) msgs)]
	(throw (Exception. errors))))
    r))

(defn token-headers [token]
  {"Token" token
   "Content-type" "application/xml"})

(defn- fetch [token url]
  (validate-response (web/get url (token-headers token))))

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

(def exact query/encode)

(defn- make-dispatcher [& pre-args]
  (fn [target & args]
    (apply target (concat pre-args args))))

(defn user [token]
  (make-dispatcher token))

(defn project [token project-id]
  "Returns a function works like apply but passing in token and project-id
  as paramaters as the first two arguments"
  (make-dispatcher token project-id))

(defn info [token project-id]
  "Returns a map with the current project settings"
  (-> (fetch token (project-url project-id))
      :response :project))

(defn projects [token]
  (-> (fetch token (project-url ""))
      :projects))

(defn iterations [token project-id]
  (-> (fetch token (str (project-url project-id) "/iterations"))
      :iterations))

(defn iterations-group [token project-id name]
  (-> (fetch token (str (project-url project-id) "/iterations/" name))
      :iterations))

(defn backlog [token project-id]
  (iterations-group token project-id "backlog"))

(defn current [token project-id]
  (iterations-group token project-id "current"))

(defn done [token project-id]
  (iterations-group token project-id "done"))

(defn all 
  "Return a lazy seqs of story maps given an optional list of filters"
  [token project-id & filter]
  (let [url (str (story-url project-id) 
		 (if filter 
		   (str "?filter=" (apply query/combine filter))))]
    (map :story 
	 (-> (fetch token url) :stories))))

(defn add
  "Takes a story map and adds it to the project.  
  Returns the fully populated story.
  Story map requires a :name and :requested_by."
  [token project-id story]
  (let [url (story-url project-id)
	data (xml/to-xml :story story)]
    ;  (except/throw-if (not (valid-story? story)) "Invalid story")
    (-> (validate-response (web/post url data (token-headers token))) 
	:story)))

(defn update [token project-id story-id update]
  (let [url (story-url project-id story-id)
	data (xml/to-xml :story update)]
    (validate-response (web/put url data (token-headers token)))))

(defn delete [token project-id story-id]
  "Deletes the story with story-id. Returns the response message string"
  (let [url (story-url project-id story-id)
	response (validate-response (web/delete url (token-headers token)))]
    (-> response :story)))

(defn deliver-finished-stories [token project-id]
  "Mark all finished stories in the project as delivered."
  (let [url (str (project-url project-id) 
		    "/stories_deliver_all_finished")]
    (validate-response (web/put url (token-headers token)))))

(defn points [stories]
  "Returns the sum the estimates of the story"
  (let [f (fn [result story]
	    (let [estimate (Integer/decode (story :estimate))]
	      (if (> estimate 0)
		(+ result estimate)
		result)))]
    (reduce f 0 stories)))

(defn reduce-stories [stories & keywords]
  "Returns a seq of seqs of the values of the keys for the given stories"
  (reduce #(conj %1 (map %2 keywords)) [] stories))