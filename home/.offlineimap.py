import subprocess

#stolen from emacswiki

def get_output(cmd):
    # Bunch of boilerplate to catch the output of a command:
    pipe = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
    (output, errout) = pipe.communicate()
    assert pipe.returncode == 0
    thing = output.strip().split("\n")[-1].strip('"')
    return thing


def get_password_emacs(host, port):
    cmd = "emacsclient -a \"\" --eval '(offlineimap-get-password \"%s\" \"%s\")'" % (host,port)
    return get_output(cmd).strip().lstrip('"').rstrip('"')

def get_user_emacs(host, port):
    cmd = "emacsclient -a \"\" --eval '(offlineimap-get-login \"%s\" \"%s\")'" % (host,port)
    return get_output(cmd).strip().lstrip('"').rstrip('"')

# stolen from http://roland.entierement.nu/blog/2010/09/08/gnus-dovecot-offlineimap-search-a-howto.html

import offlineimap.imaputil as IU
if not hasattr(IU, 'monkeypatchdone'):
    IU.flagmap += [('gnus-expire','E'),
                   ('gnus-dormant', 'Q'),
                   ('gnus-save', 'V'),
                   ('gnus-forward', 'W')]
    IU.monkeypatchdone = True

# Grab some folders first, and archives later
high = ['^important$', '^work$']
low = ['^archives', '^spam$']
import re

def lld_cmp(x, y):
    for r in high:
        xm = re.search (r, x)
        ym = re.search (r, y)
        if xm and ym:
            return cmp(x, y)
        elif xm:
            return -1
        elif ym:
            return +1
    for r in low:
        xm = re.search (r, x)
        ym = re.search (r, y)
        if xm and ym:
            return cmp(x, y)
        elif xm:
            return +1
        elif ym:
            return -1
    return cmp(x, y)

