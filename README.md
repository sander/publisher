To develop, open a repl:

    lein repl

This also starts a browser repl and recompiles the html file with the right
settings. To connect vim:

    :Piggieback publisher.core/repl-env

Now stuff in both .clj and .cljs can be eval’d.

To develop the server, run:

    lein cljsbuild auto server
    node server.js

To develop the client, run:

    lein figwheel

To rebuild html or css, run from clj repl, within publisher.core ns:

    (build)
