#!/usr/bin/perl -w

$sendmail = "/usr/sbin/sendmail -t";
$version = '2.1';
require "cgi-lib.pl";
&ReadParse;

$REMOTE_ADDR = $ENV{REMOTE_ADDR};
$REMOTE_HOST = $ENV{REMOTE_HOST};
$REMOTE_USER = $ENV{REMOTE_USER};
$SERVER_NAME = $ENV{SERVER_NAME};
$REMOTE_URL = $ENV{REMOTE_URL};
$HTTP_USER_AGENT = $ENV{HTTP_USER_AGENT};

$MAIL_TO = $in{'to'};
$MAIL_CC = $in{'cc'};
$MAIL_SUBJECT = "JORAM download";
$MAIL_EMAIL = $in{'Email'};
$MAIL_NAME = $in{'Name'};
$MAIL_REALEMAIL = $ENV{'REMOTE_HOST'} ? $ENV{'REMOTE_HOST'}: $ENV{'REMOTE_ADDR'};
$MAIL_MESSAGE = $in{'body'};
$MAIL_COMPANY = $in{'Company'};
$nexturl  = $in{'nexturl'};

print "Content-type: text/html\n\n";
print "\n\n";

print "<HTML>\n";
print "<HEAD>\n";
print "<TITLE>JORAM download</TITLE>";
print "<STYLE type=\"text/css\">";
print "A:active {color: #EF5F00; background: transparent; text-decoration: underline;}";
print "A:link {background: transparent; text-decoration: underline;}";
print "A:visited {background: transparent; text-decoration: underline;}";
print "A:hover {color: #EF5F00; background: transparent; text-decoration: underline;}";
print "</STYLE>";
print "</HEAD>\n";

print "<BODY VLINK=\"#525D76\" ALINK=\"#EF5F00\" LINK=\"#525D76\" TEXT=\"#000000\" BGCOLOR=\"#ffffff\">\n";

# header
print "<h1><A HREF=\"../../joram/index.html\"><IMG SRC=\"../../joram/images/joram.jpg\" ALT=\"JORAM logo\" HEIGHT=\"132\" WIDTH=\"287\" BORDER=\"0\" HSPACE=\"5\" ALIGN=\"middle\"></A>JORAM download</h1>\n";
print "<HR>\n";

$MESSAGE = "";
if  ($MAIL_NAME eq "") {
  $MESSAGE="Please indicate your name.";
}

if ($MAIL_EMAIL =~/^.+\@.+/) {}
else {
  if  ($MAIL_EMAIL eq "") {
    $MESSAGE=$MESSAGE." Please indicate your e-mail address.";
  } else {
    $MESSAGE=$MESSAGE." The e-mail address is not correct.";
  }
}

if  ($MAIL_COMPANY eq "") {
  $MESSAGE=$MESSAGE." Please indicate the name of your company.";
}

if ($MESSAGE eq "") {
  &send_mail;

  print "<P>Thank you for your input, which we have recorded as follows:</P>\n";
  print "<P>";
  print "Name:    $MAIL_NAME<BR>\n";
  print "E-mail:  $MAIL_EMAIL<BR>\n";
  print "Company: $MAIL_COMPANY";
  print "</P>\n";

  print "<HR>\n";

  print "<H2>Latest release</H2>\n";

  print "<H3>JORAM 2.2</H3>\n";
  print "<UL>\n";
  print "<LI>JORAM 2.2 jars (<A HREF=\"../../../joram/download/joram-2.2-bin.tar.gz\">tar.gz</A>, <A HREF=\"../../../joram/download/joram-2.2-bin.zip\">zip</A>)</li>\n";
  print "<LI>JORAM 2.2 sources (<A HREF=\"../../../joram/download/joram-2.2-src.tar.gz\">tar.gz</A>, <A HREF=\"../../../joram/download/joram-2.2-src.zip\">zip</A>)</li>\n";
  print "</UL>\n";

  print "<HR>\n";

  print "<H2>Old releases</H2>\n";

  print "<H3>JORAM 2.1.1</H3>\n";
  print "<UL>\n";
  print "<LI>JORAM 2.1.1 jars (<A HREF=\"../../../joram/download/joram-2.1.1-bin.tar.gz\">tar.gz</A>, <A HREF=\"../../../joram/download/joram-2.1.1-bin.zip\">zip</A>)</li>\n";
  print "<LI>JORAM 2.1.1 sources (<A HREF=\"../../../joram/download/joram-2.1.1-src.tar.gz\">tar.gz</A>, <A HREF=\"../../../joram/download/joram-2.1.1-src.zip\">zip</A>)</li>\n";
  print "</UL>\n";

  print "<HR>\n";

  print "<P>Please see the <A HREF=\"../../../joram/current/doc/install/index.html\">Installation Guide</A> that is part of the distribution.</p>";
}

else {
  $MESSAGE=$MESSAGE." Please try again.";
  print "<P><FONT COLOR=\"red\">$MESSAGE</FONT></P>\n";
}

# footer
print "<HR>\n";
print "<P><A HREF=\"../../joram/index.html\">JORAM homepage</A> - Copyright © 1999-2002, <A HREF=\"../../index.html\">ObjectWeb Consortium</A>.</P>";

print "</BODY>\n";
print "</HTML>\n";


sub send_mail {
  open(EMAIL,"| $sendmail");
  if ($MAIL_CC) {
    print EMAIL "Cc: $MAIL_CC\n"
  }
  if ($MAIL_EMAIL eq "") {
    print EMAIL "From: $MAIL_NAME\n";
  } else {
    print EMAIL "From: $MAIL_NAME <$MAIL_EMAIL>\n";
  }
  print EMAIL "To: $MAIL_TO\n";
  print EMAIL "Reply-To: $MAIL_EMAIL\n";
  print EMAIL "Errors-To: $MAIL_EMAIL\n";
  print EMAIL "Sender: $MAIL_EMAIL\n";
  print EMAIL "Subject: $MAIL_SUBJECT\n";
  print EMAIL "X-Mail-Gateway: WWW Mail Gateway $version\n";
  print EMAIL "X-Real-Host-From: $MAIL_REALEMAIL\n\n";
  print EMAIL "==========================================================\n";
  print EMAIL "JORAM download\n";
  print EMAIL "==========================================================\n";
  print EMAIL "Name:    $MAIL_NAME\n";
  print EMAIL "Email:   $MAIL_EMAIL\n";
  print EMAIL "Company: $MAIL_COMPANY\n";
  print EMAIL "Host:    $REMOTE_HOST\n";
  print EMAIL "IP addr: $REMOTE_ADDR\n";
  print EMAIL "User:    $REMOTE_USER\n";
  print EMAIL "Server:  $SERVER_NAME\n";
  print EMAIL "URL:     $REMOTE_URL\n";
  print EMAIL "Browser: $HTTP_USER_AGENT\n";
  print EMAIL "==========================================================\n";
  close (EMAIL);
}

sub ReadParse {
  local (*in) = @_ if @_;
  local ($i, $key, $val);

  # Read in text
  if (&MethGet) {
    $in = $ENV{'QUERY_STRING'};
  } elsif (&MethPost) {
    read(STDIN,$in,$ENV{'CONTENT_LENGTH'});
  }

  @in = split(/[&;]/,$in); 

  foreach $i (0 .. $#in) {
    # Convert plus's to spaces
    $in[$i] =~ s/\+/ /g;

    # Split into key and value.  
    ($key, $val) = split(/=/,$in[$i],2); # splits on the first =.

    # Convert %XX from hex numbers to alphanumeric
    $key =~ s/%(..)/pack("c",hex($1))/ge;
    $val =~ s/%(..)/pack("c",hex($1))/ge;

    # Associate key and value
    $in{$key} .= "\0" if (defined($in{$key})); # \0 is the multiple separator
    $in{$key} .= $val;

  }
  return scalar(@in);
}

# MethGet
# Return true if this cgi call was using the GET request, false otherwise
sub MethGet {
  return ($ENV{'REQUEST_METHOD'} eq "GET");
}

# MethPost
# Return true if this cgi call was using the POST request, false otherwise
sub MethPost {
  return ($ENV{'REQUEST_METHOD'} eq "POST");
}

# MyURL
# Returns a URL to the script
sub MyURL  {
  local ($port);
  $port = ":" . $ENV{'SERVER_PORT'} if  $ENV{'SERVER_PORT'} != 80;
  return  'http://' . $ENV{'SERVER_NAME'} .  $port . $ENV{'SCRIPT_NAME'};
}

# CgiError
# Prints out an error message which which containes appropriate headers,
# markup, etcetera.
# Parameters:
#  If no parameters, gives a generic error message
#  Otherwise, the first parameter will be the title and the rest will 
#  be given as different paragraphs of the body
sub CgiError {
  local (@msg) = @_;
  local ($i,$name);

  if (!@msg) {
    $name = &MyURL;
    @msg = ("Error: script $name encountered fatal error");
  };

  print &PrintHeader;
  print "<html><head><title>$msg[0]</title></head>\n";
  print "<BODY>";
  print "<h1>$msg[0]</h1>\n";
  foreach $i (1 .. $#msg) {
    print "<p>$msg[$i]</p>\n";
  }
  print "</body></html>\n";
}

# CgiDie
# Identical to CgiError, but also quits with the passed error message.
sub CgiDie {
  local (@msg) = @_;
  &CgiError (@msg);
  die @msg;
}

# PrintVariables
# Nicely formats variables in an associative array passed as a parameter
# And returns the HTML string.
sub PrintVariables {
  local (%in) = @_;
  local ($old, $out, $output);
  $old = $*;  $* =1;
  $output .=  "\n<dl compact>\n";
  foreach $key (sort keys(%in)) {
    foreach (split("\0", $in{$key})) {
      ($out = $_) =~ s/\n/<br>\n/g;
      $output .=  "<dt><b>$key</b>\n <dd><i>$out</i><br>\n";
    }
  }
  $output .=  "</dl>\n";
  $* = $old;

  return $output;
}

#1; #return true
