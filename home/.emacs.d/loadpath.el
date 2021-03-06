;; recently I became disillusioned with this ELPA thing.  I will be
;; configuring by hand.

(message "Loading config (doesn't do much, don't worry)")

;; stolen from dotemacs
(defun join-dirs (root &rest dirs)
  "Joins a series of directories together, like Python's os.path.join,
  (join-dirs \"/tmp\" \"a\" \"b\" \"c\") => /tmp/a/b/c"

  (if (not dirs)
      root
    (apply 'join-dirs
           (expand-file-name (car dirs) root)
           (cdr dirs))))

;; our bash stuff will configure these things correctly, but can override in elisp if desired
(unless (boundp 'autoload-dir)
  (defvar autoload-dir
    (or (getenv "EMACS_AUTOLOAD_DIR")
	(join-dirs  (getenv "HOME") ".emacs.d" "autoload"))))

;; stolen from prelude
(defun load-personal-config ()
  "Loads all .el files in autoload-dir"
  (when (file-exists-p autoload-dir)
    (message "Loading personal configuration files in %s..." autoload-dir)
    (mapc 'load (directory-files autoload-dir 't "^[^#].*el$"))))
(add-to-list 'load-path
  (join-dirs (getenv "HOME") ".emacs.d" "autoload" "ecb"))
(add-to-list 'load-path
  (join-dirs (getenv "HOME") ".emacs.d" "autoload" "scala-mode2"))
(add-to-list 'load-path
  (join-dirs (getenv "HOME") ".emacs.d" "autoload" "ensime" "elisp"))
(add-to-list 'load-path
  (join-dirs (getenv "HOME") ".emacs.d" "autoload" "lua-mode"))
(add-to-list 'load-path
  (join-dirs (getenv "HOME") ".emacs.d" "autoload" "go-mode"))
(load-personal-config)
;; figure out how to make this not suck
