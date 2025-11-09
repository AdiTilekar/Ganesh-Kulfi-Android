package com.ganeshkulfi.app.presentation.viewmodel;

import com.ganeshkulfi.app.data.repository.AuthRepository;
import com.ganeshkulfi.app.data.repository.InventoryRepository;
import com.ganeshkulfi.app.data.repository.PricingRepository;
import com.ganeshkulfi.app.data.repository.RetailerRepository;
import com.ganeshkulfi.app.data.repository.StockTransactionRepository;
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
public final class AdminViewModel_Factory implements Factory<AdminViewModel> {
  private final Provider<InventoryRepository> inventoryRepositoryProvider;

  private final Provider<RetailerRepository> retailerRepositoryProvider;

  private final Provider<StockTransactionRepository> stockTransactionRepositoryProvider;

  private final Provider<PricingRepository> pricingRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public AdminViewModel_Factory(Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<RetailerRepository> retailerRepositoryProvider,
      Provider<StockTransactionRepository> stockTransactionRepositoryProvider,
      Provider<PricingRepository> pricingRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.inventoryRepositoryProvider = inventoryRepositoryProvider;
    this.retailerRepositoryProvider = retailerRepositoryProvider;
    this.stockTransactionRepositoryProvider = stockTransactionRepositoryProvider;
    this.pricingRepositoryProvider = pricingRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AdminViewModel get() {
    return newInstance(inventoryRepositoryProvider.get(), retailerRepositoryProvider.get(), stockTransactionRepositoryProvider.get(), pricingRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static AdminViewModel_Factory create(
      Provider<InventoryRepository> inventoryRepositoryProvider,
      Provider<RetailerRepository> retailerRepositoryProvider,
      Provider<StockTransactionRepository> stockTransactionRepositoryProvider,
      Provider<PricingRepository> pricingRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new AdminViewModel_Factory(inventoryRepositoryProvider, retailerRepositoryProvider, stockTransactionRepositoryProvider, pricingRepositoryProvider, authRepositoryProvider);
  }

  public static AdminViewModel newInstance(InventoryRepository inventoryRepository,
      RetailerRepository retailerRepository, StockTransactionRepository stockTransactionRepository,
      PricingRepository pricingRepository, AuthRepository authRepository) {
    return new AdminViewModel(inventoryRepository, retailerRepository, stockTransactionRepository, pricingRepository, authRepository);
  }
}
