(require 'netrc)
(defun offlineimap-get (host port token)
  (let* ((netrc (netrc-parse (expand-file-name "~/.authinfo.gpg")))
	  (hostentry (netrc-machine netrc host port port)))
    (when hostentry (netrc-get hostentry token))))

(defun offlineimap-get-password (host port)
  "helper for offlineimap"
  (offlineimap-get host port "password"))

(defun offlineimap-get-login (host port)
  "helper for offlineimap"
  (offlineimap-get host port "login"))

; Shortcut for offlineimap.el running
(eval-after-load "gnus"
  '(progn
     (define-key gnus-group-mode-map (kbd "v g")
       (lambda ()
         (interactive)
         (offlineimap)))))

;; Add a hook to get news after offlineimap is finished
(add-hook 'offlineimap-event-hooks (lambda (msg-type &optional action)
                                     (if (equal "finished\n" msg-type)
                                         (gnus-group-get-new-news))))