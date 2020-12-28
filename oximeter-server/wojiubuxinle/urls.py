from django.urls import path
from nihaigeiwochongminshishi import views

urlpatterns = [
    path(r'oximeter/', views.oximeter, name='oximeter'),
    path(r'thermometer/', views.thermometer, name='thermometer'),
    path(r'acquire_oximeter/', views.acquire_oximeter, name='acquire_oximeter'),
    path(r'acquire_thermometer/', views.acquire_thermometer, name='acquire_thermometer')
]
