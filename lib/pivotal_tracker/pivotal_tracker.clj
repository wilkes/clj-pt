(ns pivotal-tracker
  (:require (clojure.contrib (except :as except))
	    (pivotal-tracker (xml :as xml)))
  (:import (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet HttpPost HttpDelete HttpPut)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.entity StringEntity)
	   (org.apache.http.util EntityUtils)))

; URL creation
(def *base-url* "http://www.pivotaltracker.com/services/v1/projects/")

(defn project-url [project-id]
  (str *base-url* project-id))

(defn story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn build-request
  ([token request]
     (doto request
       (addHeader "Token" token)
       (addHeader "Content-type" "application/xml")))
  ([token request data]
     (doto (build-request token request)
       (setEntity (StringEntity. data)))))

(defn do-request [request]
  (.execute (DefaultHttpClient.) request))

(defn fetch-url [token url]
  (do-request (build-request token (HttpGet. url))))

(defn post-url [token url data]
  (do-request (build-request token (HttpPost. url) data)))

(defn response-to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn fetch-as-xml [token url]
  (-> (fetch-url token url) response-to-string xml/parse))

(defn fetch-item [token url]
  (xml/to-struct (-> (fetch-as-xml token url) :content first)))

(defn fetch-collection [token url]
  (reduce #(cons (xml/to-struct %2) %1) 
	  [] (-> (fetch-as-xml token url) :content second :content)))

;; Get Project
(defn get-project [token project-id]
  (fetch-item token (project-url project-id)))

;; Getting Stories
;;   Single story
(defn get-story [token project-id story-id]
  (fetch-item token (story-url project-id story-id)))

;;   All stories in a project
(defn get-all-stories [token project-id]
  (fetch-collection token (story-url project-id)))

;;   Stories by filter
;; Currently the client is responsbile for encoding 
(defn search-stories [token project-id criteria]
  (fetch-collection token (str (story-url project-id) "?filter=" criteria)))

;; Adding stories
(defn valid-story? [s]
  (let [required #{:name :requested_by}
	optional #{:id :story_type :url :estimate :current_state :description :created_at}
	all-fields (concat required optional)]
    (and
     (every? #(contains? s %) required)
     (every? #(.contains all-fields %) (keys s)))))

(defn add-story [token project-id story]
  (except/throw-if (not (valid-story? story)) "Invalid story")
  (post-url token
	    (story-url project-id)
	    (xml/to-xml :story story)))

;; Updating Stories
;; Always getting a 500?!?!
(defn update-story [token project-id story-id update]
  (post-url token
	    (story-url project-id story-id)
	    :story update))

;; Deleting Stories
(defn delete-story [token project-id story-id]
  (do-request (build-request token (HttpDelete. (story-url project-id story-id)))))

;; Deliver All Finished Stories
(defn deliver-finished-stories [token project-id]
  (do-request
   (build-request token (HttpPut. (str (project-url project-id) 
				       "/stories_deliver_all_finished")))))

