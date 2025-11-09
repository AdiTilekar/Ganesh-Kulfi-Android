package com.ganeshkulfi.app.data.repository;

import android.content.SharedPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<SharedPreferences> sharedPreferencesProvider;

  public AuthRepository_Factory(Provider<SharedPreferences> sharedPreferencesProvider) {
    this.sharedPreferencesProvider = sharedPreferencesProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(sharedPreferencesProvider.get());
  }

  public static AuthRepository_Factory create(
      Provider<SharedPreferences> sharedPreferencesProvider) {
    return new AuthRepository_Factory(sharedPreferencesProvider);
  }

  public static AuthRepository newInstance(SharedPreferences sharedPreferences) {
    return new AuthRepository(sharedPreferences);
  }
}
