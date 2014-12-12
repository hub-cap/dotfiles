	(setq mm-discouraged-alternatives '("text/html" "text/richtext")) ; prefer plaintext

	;; some smtp stuff stolen from http://www.emacswiki.org/emacs/GnusMSMTP
	(setq gnus-novice-user nil)
	(setq message-send-mail-function 'message-send-mail-with-sendmail)
	(setq sendmail-program "/usr/bin/msmtp")
	(setq message-sendmail-extra-arguments '("-a" "gmail"))
	(setq mail-host-address "gmail.com")
	(setq user-full-name "Michael Basnight")
	(setq user-mail-address "mbasnight@gmail.com")
	(setq message-kill-buffer-on-exit t)
	;; using cg-feed-msmtp to set msmtp stuff, using posting-style to set from address
	(setq gnus-parameters
	  ;;Use notthere id for all gmane news group postings
	  '((".*gmail.*"
	     (posting-style
	      (address "mbasnight@gmail.com")
	      (name "Michael Basnight")
	;      (eval (setq message-sendmail-extra-arguments '("-a" "gmail")))
	      (user-mail-address "mbasnight@gmail.com")))))

	(setq gnus-select-method '(nntp "news.gnus.org"))
	(setq gnus-secondary-select-methods '((nnimap "gmail"
						      (nnimap-stream shell)
						      (nnimap-shell-program
							"/usr/lib/dovecot/imap -o mail_location=maildir:$HOME/Maildir/mbasnight@gmail.com"))
					      (nntp "news.gmane.org")
					      (nntp "news.gwene.org")))

	;; Choose account label to feed msmtp -a option based on From header in Message buffer;
	;; This function must be added to message-send-mail-hook for on-the-fly change of From address
	;; before sending message since message-send-mail-hook is processed right before sending message.
	(defun cg-feed-msmtp ()
	  (if (message-mail-p)
	      (save-excursion
		(let* ((from
			(save-restriction
			    (message-narrow-to-headers)
			      (message-fetch-field "from")))
			      (account
			       (cond
				  ((string-match "mbasnight@gmail\.com" from) "gmail"))))
		    (setq message-sendmail-extra-arguments (list '"-a" account)))))) ; the original form of this script did not have the ' before "a" which causes a very difficult to track bug --frozencemetery
	(setq message-sendmail-envelope-from 'header)
	(add-hook 'message-send-mail-hook 'cg-feed-msmtp)

	(require 'epg-config)
	 (setq mml2015-use 'epg

	       mml2015-verbose t
	       epg-user-id "Michael Basnight <mbasnight@gmail.com>"
	       mml2015-encrypt-to-self t
	       mml2015-always-trust nil
	       mml2015-cache-passphrase t
	       mml2015-passphrase-cache-expiry '36000
	       mml2015-sign-with-sender t

	       gnus-message-replyencrypt t
	       gnus-message-replysign t
	       gnus-message-replysignencrypted t
	       gnus-treat-x-pgp-sig t

	;;       mm-sign-option 'guided
	;;       mm-encrypt-option 'guided
	       mm-verify-option 'always
	       mm-decrypt-option 'always

	       gnus-buttonized-mime-types
	       '("multipart/alternative"
		 "multipart/encrypted"
		 "multipart/signed")

	      epg-debug t ;;  then read the *epg-debug*" buffer
	)

	(add-hook 'message-setup-hook 'mml-secure-sign-pgpmime)
	(eval-after-load "gnus"
	  '(progn
	     (define-key gnus-summary-mode-map (kbd "v d")
	       (lambda ()
		  (interactive)
		   (gnus-summary-delete-article)))))
