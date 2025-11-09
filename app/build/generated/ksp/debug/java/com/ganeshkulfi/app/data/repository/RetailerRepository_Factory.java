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
public final class RetailerRepository_Factory implements Factory<RetailerRepository> {
  @Override
  public RetailerRepository get() {
    return newInstance();
  }

  public static RetailerRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RetailerRepository newInstance() {
    return new RetailerRepository();
  }

  private static final class InstanceHolder {
    private static final RetailerRepository_Factory INSTANCE = new RetailerRepository_Factory();
  }
}
