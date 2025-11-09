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
public final class FlavorRepository_Factory implements Factory<FlavorRepository> {
  @Override
  public FlavorRepository get() {
    return newInstance();
  }

  public static FlavorRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FlavorRepository newInstance() {
    return new FlavorRepository();
  }

  private static final class InstanceHolder {
    private static final FlavorRepository_Factory INSTANCE = new FlavorRepository_Factory();
  }
}
