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

(defn to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn to-xml [response]
  (-> response to-string xml/parse))

(defn get [token url]
  (println url)
  (do-request token (HttpGet. url)))

(defn get-xml [token url]
  (to-xml (get token url)))

(defn post [token url data]
  (do-request token (HttpPost. url) data))

(defn delete [token url]
  (do-request token (HttpDelete. url)))

(defn put [token url]
  (do-request token (HttpPut. url)))

