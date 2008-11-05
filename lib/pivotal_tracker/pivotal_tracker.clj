(ns pivotal-tracker
  (:require [clojure.xml :as xml])
  (:import (java.io ByteArrayInputStream)
	   (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.util EntityUtils)))

(def *base-url* "http://www.pivotaltracker.com/services/v1/projects/")

(defn- project-url [project-id]
  (str *base-url* project-id))

(defn- story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn fetch-url [token url]
  (let [get (doto (HttpGet. url)
	     (addHeader "Token" token))]
    (.execute (DefaultHttpClient.) get)))

(defn response-to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn parse-xml-string [s]
  (xml/parse (ByteArrayInputStream. (.getBytes s))))

(defn fetch-xml [token url]
  (-> (fetch-url token url) response-to-string parse-xml-string))

;; Get Project
(defn get-project [token project-id]
  (fetch-xml token (project-url project-id)))

;; Getting Stories
;;   Single story
(defn get-story [token project-id story-id]
  (fetch-xml token (story-url project-id story-id)))

;;   All stories in a project
(defn get-all-stories [token project-id]
  (fetch-xml token (story-url project-id)))
;;   Stories by filter
;; Adding stories
;; Updating Stories
;; Deleting Stories
;; Deliver All Finished Stories

