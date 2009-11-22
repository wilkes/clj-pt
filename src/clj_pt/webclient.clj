(ns clj-pt.webclient
  (:refer-clojure :exclude [get])
  (:import (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet HttpPost HttpDelete HttpPut)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.entity StringEntity)
	   (org.apache.http.util EntityUtils)))

(defn- add-headers [request headers]
  (prn headers)
  (doseq [[k v] headers]
    (.addHeader request k v)))

(defn- add-body [request data]
  (.setEntity request (StringEntity. data)))

(defn- exec-req [headers request]
  (let [client (DefaultHttpClient.)
        request (doto request (add-headers (first headers)))
        response (EntityUtils/toString
                  (.getEntity (.execute client request)))]
    (.. client getConnectionManager shutdown)
    response))

(defn get [url & headers]
  (exec-req headers (HttpGet. url)))

(defn post [url data & headers]
  (exec-req headers (doto (HttpPost. url) (add-body data))))

(defn delete [url & headers]
  (exec-req headers (HttpDelete. url)))

(defn put [url data & headers]
  (exec-req headers (doto (HttpPut. url) (add-body data))))

