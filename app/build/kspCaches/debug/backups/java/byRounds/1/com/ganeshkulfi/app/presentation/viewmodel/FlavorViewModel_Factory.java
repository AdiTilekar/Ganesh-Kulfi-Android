package com.ganeshkulfi.app.presentation.viewmodel;

import com.ganeshkulfi.app.data.repository.FlavorRepository;
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
public final class FlavorViewModel_Factory implements Factory<FlavorViewModel> {
  private final Provider<FlavorRepository> flavorRepositoryProvider;

  public FlavorViewModel_Factory(Provider<FlavorRepository> flavorRepositoryProvider) {
    this.flavorRepositoryProvider = flavorRepositoryProvider;
  }

  @Override
  public FlavorViewModel get() {
    return newInstance(flavorRepositoryProvider.get());
  }

  public static FlavorViewModel_Factory create(
      Provider<FlavorRepository> flavorRepositoryProvider) {
    return new FlavorViewModel_Factory(flavorRepositoryProvider);
  }

  public static FlavorViewModel newInstance(FlavorRepository flavorRepository) {
    return new FlavorViewModel(flavorRepository);
  }
}
