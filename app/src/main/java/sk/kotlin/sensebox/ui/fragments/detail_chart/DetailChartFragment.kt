package sk.kotlin.sensebox.ui.fragments.detail_chart

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import kotlinx.android.synthetic.main.fragment_detail_chart.*
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.bl.vm.DetailActivityViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment
import sk.kotlin.sensebox.utils.ValueInterpreter

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class DetailChartFragment : BaseFragment<DetailActivityViewModel>() {

    companion object {
        fun getFragment(): DetailChartFragment {
            return DetailChartFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_detail_chart

    override fun setViewModel(): DetailActivityViewModel? {
        return activity?.let {
            ViewModelProviders.of(it, viewModelFactory).get(DetailActivityViewModel::class.java)
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {

        initChart()
        observeLiveData()
    }

    private fun initChart() {
        chart_records.apply {
            setNoDataText(getString(R.string.chart_no_items))
            setNoDataTextColor(ContextCompat.getColor(context, R.color.cc_rd_800))
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setPinchZoom(true)
            setBackgroundColor(ContextCompat.getColor(context, R.color.cc_bg_100))
            isHighlightPerDragEnabled = false
            isHighlightPerTapEnabled = false
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            description.isEnabled = false

            xAxis.apply {
                setDrawGridLines(false)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                setExtraOffsets(5f, 0f, 5f, 0f)
                yOffset = 5f
            }

            axisRight.apply {
                isEnabled = false
            }

            axisLeft.apply {
                setDrawGridLines(false)
            }

            val temperatureValueFormatter = IValueFormatter { value, _, _, _ ->
                String.format("%.1f %s", value, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.TEMPERATURE_SYMBOL))
            }

            val temperatureLine = LineDataSet(null, getString(R.string.temperature)).apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = ContextCompat.getColor(context, R.color.cc_gn_800)
                setDrawValues(true)
                setDrawCircles(true)
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(context, R.color.cc_gn_800)
                valueFormatter = temperatureValueFormatter
            }

            val humidityValueFormatter = IValueFormatter { value, _, _, _ ->
                String.format("%.1f %s", value, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.HUMIDITY_SYMBOL))
            }

            val humidityLine = LineDataSet(null, getString(R.string.humidity)).apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = ContextCompat.getColor(context, R.color.cc_yw_800)
                setDrawValues(true)
                setDrawCircles(true)
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(context, R.color.cc_yw_800)
                valueFormatter = humidityValueFormatter
            }

            val lineData = LineData().apply {
                addDataSet(temperatureLine)
                addDataSet(humidityLine)
            }

            data = lineData
        }
    }

    private fun observeLiveData() {
        viewModel?.getLoadedRecords()?.observe(this, Observer { data ->
            data?.let {
                addGraphData(it)
                analyzeData(it)
            }
        })
    }

    private fun addGraphData(records: List<Record>) {
        chart_records?.lineData?.let {

            val xAxisValueFormatter = object : IAxisValueFormatter {
                override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                    return if (value >= 0 && value < records.size) {
                        ValueInterpreter.millisToUtcTime(records[value.toInt()].id.toLong())
                    } else {
                        ""
                    }
                }

                override fun getDecimalDigits() = 0
            }

            chart_records.xAxis.valueFormatter = xAxisValueFormatter

            val temperatureLine = it.getDataSetByLabel(getString(R.string.temperature), true)
            val humidityLine = it.getDataSetByLabel(getString(R.string.humidity), true)

            for (record in records) {
                it.addEntry(Entry(temperatureLine.entryCount.toFloat(), record.temperature), 0)
                it.addEntry(Entry(humidityLine.entryCount.toFloat(), record.humidity), 1)
            }

            chart_records.apply {
                notifyDataSetChanged()
                invalidate()

                setVisibleXRangeMaximum(5f)
                moveViewToAnimated(it.entryCount.toFloat(), 0f, YAxis.AxisDependency.RIGHT, (it.entryCount / 7) * 1000L)
            }
        }
    }

    private fun analyzeData(records: List<Record>) {
        val metrics = RecordsMetrics()

        for (record in records) {
            if (metrics.maxTemperature < record.temperature) {
                metrics.maxTemperature = record.temperature
            }

            if (metrics.minTemperature > record.temperature) {
                metrics.minTemperature = record.temperature
            }

            if (metrics.maxHumidity < record.humidity) {
                metrics.maxHumidity = record.humidity
            }

            if (metrics.minHumidity > record.humidity) {
                metrics.minHumidity = record.humidity
            }

            metrics.avgTemperature += record.temperature
            metrics.avgHumidity += record.humidity
        }

        metrics.avgTemperature = metrics.avgTemperature / records.size
        metrics.avgHumidity = metrics.avgHumidity / records.size

        viewBinding?.apply {
            setVariable(BR.metrics, metrics)
        }
    }

    data class RecordsMetrics(
            var maxTemperature: Float = Constants.MODULE_TEMPERATURE_MIN_VALUE,
            var minTemperature: Float = Constants.MODULE_TEMPERATURE_MAX_VALUE,
            var avgTemperature: Float = 0f,
            var maxHumidity: Float = Constants.MODULE_HUMIDITY_MIN_VALUE,
            var minHumidity: Float = Constants.MODULE_HUMIDITY_MAX_VALUE,
            var avgHumidity: Float = 0f
    )
}