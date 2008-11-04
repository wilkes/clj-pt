(ns pivotal-tracker
  (:require [clojure.xml :as xml])
  (:import (java.io ByteArrayInputStream)
	   (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.util EntityUtils)))

(defn action-url
  ([project-id]
     (str "http://www.pivotaltracker.com/services/v1/projects/" project-id))
  ([project-id action]
     (str (action-url project-id) "/" action)))

(defn fetch-url [token url]
  (let [get (doto (HttpGet. url)
	     (addHeader "Token" token))]
    (.execute (DefaultHttpClient.) get)))

(defn response-to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn parse-xml-string [s]
  (xml/parse (ByteArrayInputStream. (.getBytes s))))

;; Get Project
(defn get-project [token project-id]
  (-> (fetch-url token (action-url project-id))
      response-to-string
      parse-xml-string))

;; Getting Stories
;;   Single story
;;   All stories in a project
;;   Stories by filter
;; Adding stories
;; Updating Stories
;; Deleting Stories
;; Deliver All Finished Stories

