package com.example.tiendasuplementacion.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tiendasuplementacion.model.Order

class OrderPagingSource(
    private val repository: OrderRepository,
    private val statusId: Long? = null,
    private val search: String? = null
) : PagingSource<Int, Order>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Order> {
        val page = params.key ?: 1
        val size = params.loadSize
        return try {
            val items = repository.getPaged(page, size, statusId, search)
            val nextKey = if (items.size < size) null else page + 1
            LoadResult.Page(data = items, prevKey = if (page == 1) null else page - 1, nextKey = nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Order>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1) ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
