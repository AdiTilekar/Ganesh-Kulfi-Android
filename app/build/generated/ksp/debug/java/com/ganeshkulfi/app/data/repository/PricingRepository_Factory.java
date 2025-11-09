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
public final class PricingRepository_Factory implements Factory<PricingRepository> {
  @Override
  public PricingRepository get() {
    return newInstance();
  }

  public static PricingRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PricingRepository newInstance() {
    return new PricingRepository();
  }

  private static final class InstanceHolder {
    private static final PricingRepository_Factory INSTANCE = new PricingRepository_Factory();
  }
}
