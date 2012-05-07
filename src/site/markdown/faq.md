<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

#FAQ:

Q: The initiator / target is now working with the target / initiator of [insert cool vendor], why?

A: This can happen. The problem is, that the [RFC 3720](http://www.ietf.org/rfc/rfc3720.txt) is interpreted quite differently by different targets / initiators making of course a entirely convenient utilization difficult. Since jSCSI furthermore is a university project leaving the prototype but not reaching the productive status yet, we rely on inputs from the open-source community in such cases.

Q: How can I start the Initiator

A: Within your own application. jSCSI (as most iSCSI initiators) aim to work below even any mounting process. As a consequence, a runnable initiator makes less sense than the appliance directly in an own application resulting in the utilization as library only.

Q: Why do you use Java as implementing programming language for such a low-level protocol?

A: Why not? Java overs convenient methods to manipulate byte-buckets, sophisticated network-functionalities plus easy ways to implement multi-threaded applications.