package me.gavin.tools.notifier

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.*

abstract class RecyclerAdapter<T, B : ViewDataBinding>(val context: Context, val list: MutableList<T>) :
    RecyclerView.Adapter<RecyclerHolder<*>>() {

    abstract val layoutId: Int

    val headers = LinkedList<ViewDataBinding>()
    val footers = LinkedList<ViewDataBinding>()

    override fun getItemViewType(position: Int) = when {
        position < headers.size -> -1 - position // 以 -1 作为 header 的第一个下标
        position >= headers.size + list.size -> 1 + position - headers.size - list.size // 以 1 作为 footer 的第一个下标
        else -> 0
    }

    protected fun isItemDefault(position: Int) = getItemViewType(position) == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder<*> {
        return when {
            viewType < 0 -> RecyclerHolder(headers[-1 - viewType]) // header
            viewType > 0 -> RecyclerHolder(footers[viewType - 1]) // footer
            else -> RecyclerHolder( // 0: normal
                DataBindingUtil.inflate<B>(LayoutInflater.from(context), layoutId, parent, false)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerHolder<*>, position: Int) {
        if (isItemDefault(position)) {
            val realPosition = holder.adapterPosition - headers.size
            onBind(holder as RecyclerHolder<B>, realPosition, list[realPosition])
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isItemDefault(position)) 1 else layoutManager.spanCount
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerHolder<*>) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams ?: return
        if (lp.javaClass == RecyclerView.LayoutParams::class.java) {
            if (!isItemDefault(holder.layoutPosition)) {
                lp.width = RecyclerView.LayoutParams.MATCH_PARENT
            }
        } else if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = !isItemDefault(holder.layoutPosition) || lp.isFullSpan
        }
    }

    override fun getItemCount() = headers.size + list.size + footers.size

    protected abstract fun onBind(holder: RecyclerHolder<B>, position: Int, t: T)

    protected val Int.positionReal get() = this + headers.size

    fun notifyItemChangedReal(position: Int) {
        notifyItemChanged(position.positionReal)
    }

}

class RecyclerHolder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

//class BindingAdapter<T>(
//    context: Context,
//    list: MutableList<T>,
//    override val layoutId: Int,
//    private val callback: ((Int) -> Unit)? = null
//) : RecyclerAdapter<T, ViewDataBinding>(context, list) {
//
//    override fun onBind(holder: RecyclerHolder<ViewDataBinding>, position: Int, t: T) {
//        holder.binding.setVariable(BR.t, t)
//        holder.binding.executePendingBindings()
//        callback?.let {
//            holder.itemView.findViewById<View>(R.id.touchTarget)
//                .setOnClickListener {
//                    callback.invoke(holder.adapterPosition - headers.size)
//                }
//        }
//    }
//}