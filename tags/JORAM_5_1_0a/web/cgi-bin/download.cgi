#!/usr/bin/perl -w

use CGI;

# ------------------------------------------
# constants
# ------------------------------------------
use constant SENDMAIL  => "/usr/sbin/sendmail -t";

# ------------------------------------------
# main program
# ------------------------------------------
$query = new CGI;
$query->cgi_error && error( $query, "An error has occured: " . $query->cgi_error . ".");

# check query
$name = $query->param('name');
$email = $query->param('email');
$organization = $query->param('organization');
$file = $query->param('file');
$site = $query->param('site');
$to = $query->param('to');
&check_arguments();

# send notification email
&send_mail();

# redirect to the file to download
print $query->redirect($site . $file);

exit 0;

# ------------------------------------------
# check query arguments
# ------------------------------------------
sub check_arguments() {

  # name
  if  ( !defined($name) || ($name eq "") ) {
    error( $query, 'The name is required.' );
  }

  # email
  if  ( !defined($email) ) {
    error( $query, 'The e-mail is required.' );
  } else {
    unless ( $email =~/^.+\@.+/ ) {
      if  ($email eq "") {
	error( $query, 'The e-mail is required.' );
      } else {
	error( $query, 'The e-mail format is invalid.' );	
      }
    }
  }

  # organization
  if  ( !defined($organization) || ($organization eq "") ) {
    error( $query, 'The organization is required.' );
  }

  # to field (notification message)
  if  ( !defined($to) || ($to eq "") ) {
    error( $query, 'No email specified for notication message.' );
  }
}

# ------------------------------------------
# send notification email
# ------------------------------------------
sub send_mail() {
  open(EMAIL,"| " . SENDMAIL);

  my $from_field = $name . " <" . $email . ">";

  print EMAIL "From: $from_field\n";
  print EMAIL "To: $to\n";
  print EMAIL "Reply-To: $from_field\n";
  print EMAIL "Errors-To: $from_field\n";
  print EMAIL "Sender: $from_field\n";
  print EMAIL "Subject: JORAM download\n";
  print EMAIL "X-Real-Host-From: " . $ENV{'REMOTE_HOST'} ? $ENV{'REMOTE_HOST'} : $ENV{'REMOTE_ADDR'} . "\n\n";
  print EMAIL "=============================================================\n";
  print EMAIL "JORAM download\n";
  print EMAIL "=============================================================\n";
  print EMAIL "Name: $name\n";
  print EMAIL "Email: $email\n";
  print EMAIL "Organization: $organization\n";
  print EMAIL "Release: $file\n";
  print EMAIL "from-field: $from_field\n";

  print EMAIL "Host:    $ENV{REMOTE_HOST}\n";
  print EMAIL "IP addr: $ENV{REMOTE_ADDR}\n";
  print EMAIL "User:    $ENV{REMOTE_USER}\n";
  print EMAIL "Server:  $ENV{SERVER_NAME}\n";
  print EMAIL "URL:     $ENV{REMOTE_URL}\n";
  print EMAIL "Browser: $ENV{HTTP_USER_AGENT}\n";
  print EMAIL "=============================================================\n";
  close (EMAIL);
}

# ------------------------------------------
# send notification email
# ------------------------------------------
sub error {
    my( $q, $reason ) = @_;

    print $q->header( "text/html" );
    print $q->start_html(-title=>'JORAM download - Error');
    print $q->h1('JORAM download - Error</strong>');
    print "<p>Your request has not been proceed because the following error has occured:<br>";
    print "<font color=\'red\'>";
    print $q->i( $reason );
    print "</font>";

    # footer
    print $q->hr;
    print $q->p('<address><a href="mailto:webmaster@ow2.org">webmaster@objectweb.org</a></address>');
    print $q->end_html;

    exit 1;
}
