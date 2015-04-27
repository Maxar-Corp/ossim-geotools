# Vagrant for functional Testing

This directory contains a `Vagrantfile` that points to a Fedora based 
Vagrant Box that is already configured with `grails` and `webstest.sh`. 
These are both installed in `/usr/local` and the proper paths for _JAVA_HOME_ , _GRAILS_HOME_ etc. are already set for you.

## Getting Started

### Preparation
1. You'll need [Virtualbox](http://virtualbox.org)
2. You'll also need [Vagrant](http://vagrantup.com)

After installing both of these, you're ready to start!

### Starting the box

1. In the `ossim-geotools/vagrant` directory, use `$ vagrant up` to start the testing machine.
    - This creates a testing environment with this repo mapped to the `/ossim-geotools` directory within the virtual machine.

### Stopping the box (Equivalent to power off)
1. Use `$ vagrant halt`

#### Destroy the box
1. Use `$ vagrant destroy`
    - This wipes the box clean of everything may have done and resets it again. Don't 
      worry about your code being deleted though. Since it is in the `/vagrant` directory
      it is shared with your local machine.

## Creating and Running Tests

Within the `tilestore` directory, you'll find `test/functional`. The functional tests reside here.

You can run the tests with `$ grails test-app functional:`

The output is stored in `target/geb-reports`.
