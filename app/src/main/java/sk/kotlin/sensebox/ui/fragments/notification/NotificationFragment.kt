package sk.kotlin.sensebox.ui.fragments.notification

import android.os.Bundle
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.BaseViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

class NotificationFragment : BaseFragment<BaseViewModel>() {

    companion object {
        private const val KEY_MESSAGE = "key_message"

        fun getFragment(message: String): NotificationFragment {
            val fragment = NotificationFragment()
            val bundle = Bundle()
            bundle.putString(KEY_MESSAGE, message)
            fragment.arguments = bundle

            return fragment
        }
    }

    private lateinit var message: String

    override fun setLayout() = R.layout.fragment_notification

    override fun setViewModel(): Nothing? = null

    override fun initViews(savedInstanceState: Bundle?) {
        message = arguments?.getString(KEY_MESSAGE) ?: ""

        viewBinding?.apply {
            setVariable(BR.fragment, this@NotificationFragment)
        }
    }

    fun finish() {
        activity?.supportFragmentManager?.popBackStack()
    }

    fun getMessage() = message
}