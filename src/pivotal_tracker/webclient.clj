(ns pivotal-tracker.webclient
  (:refer-clojure :exclude (get))
  (:import (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet HttpPost HttpDelete HttpPut)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.entity StringEntity)
	   (org.apache.http.util EntityUtils)))

(defn to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn add-headers [request headers]
  (prn headers)
  (doseq [[k v] headers]
    (.addHeader request k v)))

(defn add-body [request data]
  (.setEntity request (StringEntity. data)))

(defn exec-req [request headers]
  (let [response (to-string (.execute (DefaultHttpClient.)
                                      (doto request
                                        (add-headers (first headers)))))]
    response))

(defn get [url & headers]
  (exec-req (HttpGet. url) headers))

(defn post [url data & headers]
  (exec-req (doto (HttpPost. url) (add-body data))
            headers))

(defn delete [url & headers]
  (exec-req (HttpDelete. url) headers))

(defn put [url data & headers]
  (exec-req (doto (HttpPut. url) (add-body data))
            headers))

