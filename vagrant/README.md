# Vagrant for functional Testing

This directory contains a `Vagrantfile` that points to a Vagrant Box that is
already configured with `grails` and `webstest.sh`. These are both installed
in `/usr/local` and the proper paths for JAVA_HOME, GRAILS_HOME etc. are 
already set for you.

## Usage

### Preparation
1. You'll need [Virtualbox](http://virtualbox.org)
2. You'll also need [Vagrant](http://vagrantup.com)

After installing both of these, you're ready to start!

### Starting the box

1. In the `vagrant` directory, use `$ vagrant up` to start the testing machine.

This creates a testing environment with this repo's directory mapped 
to `/ossim-geotools` within the virtual machine.

2. You can now use `$ webtest.sh` to create and run functional tests.

### Stopping the box (Equivalent to power off)
1. Use `$ vagrant halt`

#### Destroy the box
1. Use `$ vagrant destroy`

This wipes the box clean of everything may have done and resets it again. Don't 
worry about your code being deleted though. Since it is in the `/vagrant` directory
it is shared with your local machine.

