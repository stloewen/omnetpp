from omnetpp.scave import results, chart
import matplotlib.pyplot as plt

params = chart.get_properties()

# This expression selects the results (you might be able to logically simplify it)
filter_expression = params["filter"]

# The data is returned as a Pandas DataFrame
df = results.get_histograms(filter_expression, include_attrs=True, include_itervars=True)

# You can perform any transformations on the data here

print(df)

title, legend = chart.extract_label_columns(df)

df.sort_values(by=[l for i, l in legend], axis='index', inplace=True)

for t in df.itertuples(index=False):
    plt.hist(bins=t.binedges[1:], x=t.binedges[1:-1], weights=t.binvalues[1:-1], label=chart.make_legend_label(legend, t), histtype='stepfilled')

plt.title(chart.make_chart_title(df, title, legend))

plt.legend()
plt.grid()