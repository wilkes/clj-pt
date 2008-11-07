(ns pivotal-tracker
  (:require (clojure (xml :as xml))
	    (clojure.contrib (except :as except)))
  (:import (java.io ByteArrayInputStream)
	   (org.apache.http.client HttpClient)
	   (org.apache.http.client.methods HttpGet HttpPost)
	   (org.apache.http.impl.client DefaultHttpClient)
	   (org.apache.http.entity StringEntity)
	   (org.apache.http.util EntityUtils)))

;; stolen from clojure.xml but replace any println's with print's
(defn emit-element [e]
  (if (instance? String e)
    (print e)
    (do
      (print (str "<" (name (:tag e))))
      (when (:attrs e)
	(doseq attr (:attrs e)
	  (print (str " " (name (key attr)) "='" (val attr)"'"))))
      (if (:content e)
	(do
	  (print ">")
	  (doseq c (:content e)
	    (emit-element c))
	  (print (str "</" (name (:tag e)) ">")))
	(print "/>")))))

(defn emit [x]
  (println "<?xml version='1.0' encoding='UTF-8'?>")
  (emit-element x))

(def *base-url* "http://www.pivotaltracker.com/services/v1/projects/")

(defn project-url [project-id]
  (str *base-url* project-id))

(defn story-url 
  ([project-id]
     (str (project-url project-id) "/stories"))
  ([project-id story-id]
     (str (story-url project-id) "/" story-id)))

(defn add-headers [token url]
  (doto url
    (addHeader "Token" token)
    (addHeader "Content-type" "application/xml")))

(defn fetch-url [token url]
  (let [req (add-headers token (HttpGet. url))]
    (.execute (DefaultHttpClient.) req)))

(defn post-url [token url data]
  (let [req (doto (add-headers token (HttpPost. url))
	      (setEntity (StringEntity. data)))]
    (.execute (DefaultHttpClient.) req)))

(defn response-to-string [response]
  (EntityUtils/toString (.getEntity response)))

(defn parse-xml-string [s]
  (xml/parse (ByteArrayInputStream. (.getBytes s))))

(defn fetch-xml [token url]
  (-> (fetch-url token url) response-to-string parse-xml-string))

(defn add-item [result item]
  (let [k (item :tag) 
	v (-> item :content first)]
    (merge result {k v})))

(defn xml-to-struct [x]
  (reduce add-item {} (-> x :content)))

(defn fetch-item [token url]
  (xml-to-struct (-> (fetch-xml token url) :content first)))

(defn fetch-collection [token url]
  (let [response-xml (fetch-xml token url)
	f (fn [r i] (cons (xml-to-struct i) r))]
    (reduce #(cons (xml-to-struct %2) %1) [] (-> response-xml :content second :content))))

(defn xmlify [hmap]
  (let [f (fn [r [key val]] (cons {:tag key :content [val]} r))]
    (reduce f [] hmap)))

(defn struct-to-xml [name hmap]
  (with-out-str (emit {:tag name :content (xmlify hmap)})))

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
	    (struct-to-xml :story story)))

;; Updating Stories
;; Deleting Stories
;; Deliver All Finished Stories

