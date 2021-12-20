package com.blloc.notification.ui.notificationlist.recycler

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.blloc.notification.databinding.ViewHolderNotifyBinding
import com.blloc.notification.domain.entities.Notification
import java.time.format.DateTimeFormatter

class NotifyAdapter :
    ListAdapter<Notification, NotifyAdapter.NotifyViewHolder>(DiffCallback()) {

    class NotifyViewHolder(private val binding: ViewHolderNotifyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Notification) = with(binding) {
            appName.text = appName.context.getAppNameFromPackageName(item.appPackage)
            notifyText.text = item.text
            appIcon.load(appIcon.context.getAppIconFromPackageName(item.appPackage))

            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            date.text = item.date.format(dateFormatter)
            time.text = item.date.format(timeFormatter)
        }

        private fun Context.getAppNameFromPackageName(packageName: String) =
            packageManager.runCatching {
                getApplicationInfo(packageName, 0)
            }.getOrNull()?.let(packageManager::getApplicationLabel)

        private fun Context.getAppIconFromPackageName(packageName: String): Drawable? {
            return packageManager.runCatching { getApplicationIcon(packageName) }.getOrNull()
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(
            oldItem: Notification,
            newItem: Notification
        ): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(
            oldItem: Notification,
            newItem: Notification
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifyViewHolder {
        val binding =
            ViewHolderNotifyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotifyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}