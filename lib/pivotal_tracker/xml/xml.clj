(ns pivotal-tracker.xml
  (:require (clojure.xml))
  (:import (java.io ByteArrayInputStream)))

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

(defn to-struct [x]
  (reduce (fn [result item]
	    (merge result 
		   {(item :tag) (-> item :content first)}))
	  {}
	  (-> x :content)))

(defn to-xml [name hmap]
  (let [content (reduce (fn [result [key val]]
			 (cons {:tag key :content [val]} result)) 
			[]
			hmap)]
    (with-out-str (emit {:tag name :content content}))))

(defn parse [s]
  (clojure.xml/parse (ByteArrayInputStream. (.getBytes s))))