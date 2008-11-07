(ns pivotal-tracker.webclient
  (:refer-clojure :exclude (get))
  (:require (clojure.contrib (except :as except))
	    (pivotal-tracker (xml :as xml)))
  (:import (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet HttpPost HttpDelete HttpPut)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.entity StringEntity)
	   (org.apache.http.util EntityUtils)))

(defn build-request
  ([token request]
     (doto request
       (addHeader "Token" token)
       (addHeader "Content-type" "application/xml")))
  ([token request data]
     (doto (build-request token request)
       (setEntity (StringEntity. data)))))

(defn do-request 
  ([token request]
      (.execute (DefaultHttpClient.) (build-request token request)))
  ([token request data]
      (.execute (DefaultHttpClient.) (build-request token request data))))

(defn response-to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn get [token url]
  (do-request token (HttpGet. url)))

(defn post [token url data]
  (do-request token (HttpPost. url) data))

(defn delete [token url]
  (do-request token (HttpDelete. url)))

(defn put [token url]
  (do-request token (HttpPut. url)))

(defn get-as-xml [token url]
  (-> (get token url) response-to-string xml/parse))
