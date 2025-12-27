package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.model.SearchResult
import com.vijay.cardkeeper.data.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<SearchResult>> =
            _searchQuery
                    .debounce(300)
                    .filter { it.length >= 2 || it.isEmpty() }
                    .flatMapLatest { query ->
                        if (query.isEmpty()) {
                            flowOf(emptyList())
                        } else {
                            searchRepository.search(query)
                        }
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
