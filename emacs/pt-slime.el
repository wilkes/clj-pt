(require 'slime)
(require 'clojure-auto)
(require 'clojure-paredit)

(add-to-list 'auto-mode-alist '("\\.clj$" . clojure-mode))

;; These are extra key defines because I kept typing them.  
;; Within clojure-mode, have Ctrl-x Ctrl-e evaluate the last 
;; expression.
;; Ctrl-c Ctrl-e is also there, because I kept typoing it.
(add-hook 'clojure-mode-hook
          '(lambda ()
             (define-key clojure-mode-map "\C-c\C-e" 'lisp-eval-last-sexp)
             (define-key clojure-mode-map "\C-x\C-e" 'lisp-eval-last-sexp)))


(setq swank-clojure-jar-path (expand-file-name "../jars/clojure.jar"))
(setq swank-clojure-extra-classpaths (directory-files "../jars" t "jar$"))
(require 'swank-clojure-autoload)
(slime)