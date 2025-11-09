package com.ganeshkulfi.app.presentation.viewmodel;

import com.ganeshkulfi.app.data.repository.AuthRepository;
import com.ganeshkulfi.app.data.repository.InventoryRepository;
import com.ganeshkulfi.app.data.repository.OrderRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class RetailerViewModel_Factory implements Factory<RetailerViewModel> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<OrderRepository> orderRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public RetailerViewModel_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<OrderRepository> orderRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.orderRepositoryProvider = orderRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public RetailerViewModel get() {
    return newInstance(inventoryRepositoryProvider.get(), orderRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static RetailerViewModel_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<OrderRepository> orderRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new RetailerViewModel_Factory(inventoryRepositoryProvider, orderRepositoryProvider, authRepositoryProvider);
  }

  public static RetailerViewModel newInstance(InventoryRepository inventoryRepository,
      OrderRepository orderRepository, AuthRepository authRepository) {
    return new RetailerViewModel(inventoryRepository, orderRepository, authRepository);
  }
}
