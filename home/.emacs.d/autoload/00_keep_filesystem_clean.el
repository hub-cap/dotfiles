;; by default, emacs poops all over the place.  We're going to make it
;; poop in wilk-conf-dir/tmp/emacs instead
(defvar my-backup-dir (join-dirs (getenv "HOME") ".emacs.d" "backup"))
(setq
   backup-by-copying t      ; don't clobber symlinks
   backup-directory-alist (cons (cons "." my-backup-dir) '()) ; don't litter my fs tree
   delete-old-versions t
   kept-new-versions 6
   kept-old-versions 2
   version-control t)       ; use versioned backups

