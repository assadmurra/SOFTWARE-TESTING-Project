"""Static web server for running the Selenium suite against the visualizer.

Same behaviour as ``python -m http.server``, with one difference: a larger
connection queue. The default queue holds only 5 pending connections; when
several browsers load the app in parallel, Windows drops the extra connections,
and Brython (which loads its .py files with synchronous requests that never time
out) then hangs on start-up. Measured: 7 of 62 page loads hung with the default
server, 0 with this one.

Usage (from anywhere):
    python selenium-tests/tools/serve.py            # serves the repository root on port 8000
    python selenium-tests/tools/serve.py 8080       # another port
"""

import functools
import http.server
import pathlib
import sys

REPOSITORY_ROOT = pathlib.Path(__file__).resolve().parents[2]


class ParallelFriendlyServer(http.server.ThreadingHTTPServer):
    request_queue_size = 128  # default is 5


def main() -> None:
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8000
    handler = functools.partial(http.server.SimpleHTTPRequestHandler, directory=str(REPOSITORY_ROOT))
    with ParallelFriendlyServer(("", port), handler) as server:
        print(f"Serving {REPOSITORY_ROOT} at http://localhost:{port}/ (Ctrl+C to stop)")
        server.serve_forever()


if __name__ == "__main__":
    main()
