#include "StudentSync.h"

// TA -> Student: invite
void StudentSync::signalStart() { helpStart_.release(); }
// TA <- Student: done
void StudentSync::waitDone() { helpDone_.acquire(); }

// Student waits until TA invites
void StudentSync::waitForStart() { helpStart_.acquire(); }
// Student tells TA they're finished
void StudentSync::signalDone() { helpDone_.release(); }
