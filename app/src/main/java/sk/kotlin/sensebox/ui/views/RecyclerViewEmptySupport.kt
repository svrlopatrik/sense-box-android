package sk.kotlin.sensebox.ui.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import sk.kotlin.sensebox.R
import timber.log.Timber


class RecyclerViewEmptySupport : RecyclerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private var emptyViewId: Int = 0
    private var emptyView: View? = null

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            val adapter = adapter
            if (adapter != null) {
                if (adapter.itemCount == 0) {
                    emptyView?.visibility = View.VISIBLE
                    visibility = View.INVISIBLE
                } else {
                    emptyView?.visibility = View.GONE
                    visibility = View.VISIBLE
                }
            }
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RecyclerViewEmptySupport)
            emptyViewId = typedArray.getResourceId(R.styleable.RecyclerViewEmptySupport_emptyView, 0)
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (emptyViewId != 0) {
            try {
                emptyView = rootView.findViewById(emptyViewId);
            } catch (exception: Exception) {
                Timber.e("Cannot find empty view.")
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
    }

    fun refresh() {
        emptyObserver.onChanged()
    }

}