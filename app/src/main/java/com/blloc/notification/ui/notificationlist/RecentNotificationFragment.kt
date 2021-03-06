package com.blloc.notification.ui.notificationlist

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blloc.notification.R
import com.blloc.notification.databinding.FragmentNotificationRecentBinding
import com.blloc.notification.domain.entities.Notification
import com.blloc.notification.ui.notificationlist.RecentNotificationViewModel.FilterNotification
import com.blloc.notification.ui.notificationlist.recycler.NotifyAdapter
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.fragment_notification_recent.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
private const val ACTION_NOTIFICATION_LISTENER_SETTINGS =
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class RecentNotificationFragment : Fragment() {

    private val enableNotificationListenerAlertDialog: AlertDialog by lazy(LazyThreadSafetyMode.NONE) {
        buildNotificationServiceAlertDialog()
    }

    private var _binding: FragmentNotificationRecentBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val notifyAdapter = NotifyAdapter()

    private val viewModel: RecentNotificationViewModel by viewModel()

    private var currentFilter: FilterNotification = FilterNotification.ALL

    private val popupBackingView: View by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().findViewById<View>(R.id.popup_backing_view)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationRecentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            recycler.run {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = notifyAdapter
            }
            trackerStopButton.setOnClickListener {
                viewModel.trackerStateToggle()
            }
            trackerStartButton.setOnClickListener {
                if (!isNotificationServiceEnabled()) {
                    enableNotificationListenerAlertDialog.show()
                    return@setOnClickListener
                }
                viewModel.trackerStateToggle()
            }
        }
        lifecycleScope.launch {
            viewModel.state
                .collect { state ->
                    render(state)
                }
        }

        checkNotificationServiceEnabledAndShowAlertIfNeeded()

        // apply insets
        binding.run {
            recycler.applySystemWindowInsetsToPadding(bottom = true)
            trackerStartButton.applySystemWindowInsetsToMargin(bottom = true)
            trackerStopButton.applySystemWindowInsetsToMargin(bottom = true)
        }
    }

    private fun checkNotificationServiceEnabledAndShowAlertIfNeeded(): Boolean {
        // If the user did not turn the notification listener service on we prompt him to do so
        return isNotificationServiceEnabled().also {
            if (it.not()) {
                enableNotificationListenerAlertDialog.show()
            }
        }
    }

    private fun render(state: RecentNotificationViewModel.UiState) {
        binding.emptyView.root.isVisible = state.isEmpty
        currentFilter = state.currentFilter
        when(currentFilter) {
            FilterNotification.ALL -> handleNotifications(state.notifications)
            FilterNotification.ACTIVE -> handleNotifications(state.activeNotifications)
        }

        binding.trackerStartButton.isVisible = !state.isTrackerEnable
        binding.trackerStopButton.isVisible = state.isTrackerEnable
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notification_tracker, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_period -> {
                showFilterPopup()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleNotifications(list: List<Notification>) {
        Timber.i("handle notification: $list")
        notifyAdapter.submitList(list) {
            if (binding.recycler.computeVerticalScrollOffset() == 0) {
                binding.recycler.scrollToPosition(0)
            }
        }
    }

    private fun showFilterPopup() {
        val view = activity?.findViewById<View>(R.id.action_period) ?: return
        PopupMenu(requireContext(), view, Gravity.END).run {
            menuInflater.inflate(R.menu.filter_notification, menu)

            val checkedItemId = when (currentFilter) {
                FilterNotification.ALL -> R.id.filter_all
                FilterNotification.ACTIVE -> R.id.filter_active

            }
            menu.findItem(checkedItemId)?.isChecked = true

            setOnMenuItemClickListener {
                val filter = when (it.itemId) {
                    R.id.filter_all -> FilterNotification.ALL
                    R.id.filter_active -> FilterNotification.ACTIVE
                    else -> return@setOnMenuItemClickListener false
                }
                viewModel.setFilter(filter)
                return@setOnMenuItemClickListener true
            }
            setOnDismissListener {
                hidePopupBackingAnimator.start()
            }
            showPopupBackingAnimator.start()
            show()
        }
    }

    private val showPopupBackingAnimator by lazy(LazyThreadSafetyMode.NONE) {
        ObjectAnimator.ofFloat(popupBackingView, View.ALPHA, 0f, 1f)
            .apply {
                addListener(onStart = {
                    popupBackingView.run {
                        isVisible = true
                    }
                })
            }
    }

    private val hidePopupBackingAnimator by lazy(LazyThreadSafetyMode.NONE) {
        ObjectAnimator.ofFloat(popupBackingView, View.ALPHA, 0f)
            .apply { addListener(onEnd = { popupBackingView.isVisible = false }) }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled
     * @return True if enabled, false otherwise.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName: String = requireContext().packageName
        val flat = Settings.Secure.getString(
            requireContext().contentResolver,
            ENABLED_NOTIFICATION_LISTENERS
        )
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        return AlertDialog.Builder(
            requireContext(),
            R.style.Theme_MaterialComponents_Light_Dialog_Alert
        )
            .setTitle(R.string.notification_listener_service)
            .setMessage(R.string.notification_listener_service_explanation)
            .setPositiveButton(R.string.enable) { _, _ ->
                startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}