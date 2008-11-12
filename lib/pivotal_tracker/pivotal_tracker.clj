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

(defn fetch-item [token url]
  (xml/to-struct (-> (client/get-xml token url) :content first)))

(defn fetch-collection [token url]
  (reduce #(cons (xml/to-struct %2) %1) 
	  [] (-> (client/get-xml token url) :content second :content)))

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

(defn get-project [token project-id]
  (fetch-item token (project-url project-id)))

(defn get-story [token project-id story-id]
  (fetch-item token (story-url project-id story-id)))

(query/criteria label requester owner mywork id)

(query/enums type
	     feature bug chore release)

(query/enums state
	     unstarted started finished 
	     delivered accepted rejected)

(defn all 
  ([token project-id & filter]
     (let [filter-str (if filter (str "?filter=" (apply query/combine filter)) "")]
       (fetch-collection token (str (story-url project-id) filter-str)))))

;; These are all returning HttpResponse objects for now
(defn add [token project-id story]
  (except/throw-if (not (valid-story? story)) "Invalid story")
  (client/post token
	    (story-url project-id)
	    (xml/to-xml :story story)))

;; Always getting a 500?!?!
(defn update [token project-id story-id update]
  (client/post token
	       (story-url project-id story-id)
	       :story update))

(defn delete [token project-id story-id]
  (client/delete token (story-url project-id story-id)))

(defn deliver-finished-stories [token project-id]
  (client/put token 
	      (str (project-url project-id) "/stories_deliver_all_finished")))

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
      (map keyword stories)
      (map #(map % (seq keyword)) stories))))
