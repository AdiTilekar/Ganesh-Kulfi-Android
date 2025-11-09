package com.ganeshkulfi.app.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class StockTransactionRepository_Factory implements Factory<StockTransactionRepository> {
  @Override
  public StockTransactionRepository get() {
    return newInstance();
  }

  public static StockTransactionRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StockTransactionRepository newInstance() {
    return new StockTransactionRepository();
  }

  private static final class InstanceHolder {
    private static final StockTransactionRepository_Factory INSTANCE = new StockTransactionRepository_Factory();
  }
}
