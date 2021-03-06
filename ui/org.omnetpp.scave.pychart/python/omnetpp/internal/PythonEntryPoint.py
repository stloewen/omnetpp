import time
import sys
import os
import math
import traceback
import pickle as pl

import functools
print = functools.partial(print, flush=True)

from omnetpp.internal.TimeAndGuard import TimeAndGuard
from omnetpp.internal import Gateway

from omnetpp.scave import results
from omnetpp.scave import chart

try:
    import matplotlib as mpl
    mpl.use('module://omnetpp.internal.backend_SWTAgg')

    import pandas as pd

    pd.set_option("display.width", 500)
    pd.set_option("display.max_columns", 50)
    pd.set_option("display.max_colwidth", 50)
    pd.set_option("display.max_rows", 500)

except ImportError as e:
    print("can't import " + e.name)
    sys.exit(1)

# the print function is replaced so it will flush after each line
execContext = {
    "print": print
}


class PythonEntryPoint(object):

    class Java:
        implements = ["org.omnetpp.scave.pychart.IPythonEntryPoint"]

    def check(self):
        return True

    def setResultsProvider(self, results_provider):
        Gateway.results_provider = results_provider
        chart.results_provider = results_provider

    def setChartPropertiesProvider(self, properties_provider):
        Gateway.properties_provider = properties_provider

    def setPlotWidgetProvider(self, widget_provider):
        Gateway.widget_provider = widget_provider

    def setNativeChartPlotter(self, chart_plotter):
        Gateway.chart_plotter = chart_plotter

    # @TimeAndGuard(measureTime=False)
    def execute(self, chartInput):
        global execContext
        exec(chartInput, execContext)

    def setGlobalObjectPickle(self, name, pickle):
        global execContext
        execContext[name] = pl.loads(pickle)


if __name__ == "__main__":
    for line in sys.stdin:
        # We don't actually expect any input, this is just a simple way to wait
        # for the parent process (Java) to die.
        pass

    # print("Python process exiting...")

    Gateway.gateway.close(False, True)
    Gateway.gateway.shutdown_callback_server()
    Gateway.gateway.shutdown()

    sys.exit()
    # it should never come to this, but just to make sure:
    os._exit(1)
