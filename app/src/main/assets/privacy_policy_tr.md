# NavXS Gizlilik Politikası

Son güncelleme: 14 Eylül 2026

## 1. Genel Bilgiler

NavXS, seçilen uygulamalar için sabit düğmelerle kayan bir navigasyon katmanı görüntüleyebilen bir Android uygulamasıdır.

Bu uygulamanın geliştiricisi, kişisel verileri yalnızca uygulamanın işlevselliği için teknik olarak gerekli olan ölçüde toplar, depolar veya işler.

## 2. Cihazda Yerel İşleme

NavXS, bilgileri genellikle cihaz üzerinde yerel olarak işler.

Bu özellikle şunları içerir:

- ön planda çalışan uygulamanın paket adı
- katmanın etkinleştirilmesi gereken uygulama seçimi
- kullanıcı tarafından belirlenen katman ayarları (konumlar, renkler, boyutlar, şeffaflık ve temalar)
- erişilebilirlik hizmetinin etkin olup olmadığının durumu
- kullanıcının isteğe bağlı olarak konumlandırma düzenleyicisinin arka planı olarak seçtiği bir görüntünün URI'si ve bunun için seçilen opaklık

## 3. İşlemenin Amacı

Bu bilgiler yalnızca şu amaçlarla işlenir:

- şu an açık olan uygulama için katmanın gösterilmesi gerekip gerekmediğini algılamak
- katmanı göstermek veya gizlemek
- yapılandırılmış navigasyon eylemlerini sağlamak
- kullanıcı tarafından seçilen ayarları yerel olarak kaydetmek ve geri yüklemek

## 4. Erişilebilirlik Hizmeti

NavXS, Android Erişilebilirlik Hizmeti API'sini (AccessibilityService) kullanır. NavXS, engelli kişilere yönelik bir erişilebilirlik aracı değildir; hizmet, uygulamanın temel özelliği olan gezinme katmanı için gereklidir.

### 4.1 Onay

Erişilebilirlik hizmeti istenmeden önce NavXS, hizmetin nasıl kullanıldığını açıklayan bir bilgilendirme gösterir. Kullanıcı iki düğme arasında seçim yapar: "Reddet" ve "Kabul et". NavXS, Android erişilebilirlik ayarlarını yalnızca "Kabul et" düğmesine dokunulduktan sonra açar; kullanıcının hizmeti orada kendisi etkinleştirmesi gerekir. "Reddet" hiçbir şeyi etkinleştirmeden uygulamayı kapatır. Bilgilendirmeden başka bir şekilde ayrılmak onay olarak kabul edilmez. Hizmet, Android erişilebilirlik ayarlarından istediğiniz zaman devre dışı bırakılabilir.

### 4.2 Hizmetin kullanım amacı

Hizmet yalnızca şu amaçlarla kullanılır:

- şu anda ön planda çalışan uygulamanın paket adını algılamak
- o uygulama için katmanın gösterilip gösterilmeyeceğine karar vermek
- seçilen uygulamaların üzerinde kayan gezinme düğmelerini göstermek
- kullanıcı ilgili düğmeye dokunduğunda Geri, Ana Ekran ve Son Uygulamalar gezinme işlemlerini gerçekleştirmek

### 4.3 Hizmetin yapmadıkları

Erişilebilirlik hizmetinin teknik olarak ön plandaki uygulamanın pencere bilgilerine erişimi vardır. NavXS bu erişimi yalnızca o uygulamanın paket adını belirlemek için kullanır. NavXS; yazılan metinleri, şifreleri, mesaj içeriklerini veya diğer ekran içeriklerini okumaz, saklamaz ya da iletmez. NavXS, yukarıda belirtilen gezinme işlemleri dışında kullanıcı adına herhangi bir işlem yapmaz ve bunları da yalnızca kullanıcı bir düğmeye dokunduğunda yapar.

Bu hizmet reklam, izleme veya analiz amacıyla kullanılmaz ve veriler satılmaz ya da paylaşılmaz.

## 5. Harici Sunuculara Aktarım Yok

Şu an itibarıyla NavXS, seçili uygulamaları, ön plan uygulamasının paket adlarını veya katman ayarlarını harici sunuculara ya da diğer harici hizmetlere iletmemektedir.

İşleme cihaz üzerinde yerel olarak gerçekleşir.

## 6. Veri Depolama

NavXS tarafından depolanan ayarlar ve uygulama seçimleri cihazda yerel olarak kaydedilir.

Bu veriler, kullanıcı tarafından değiştirilene, uygulama verileri temizlenerek kaldırılana veya uygulama kaldırılarak silinene kadar cihazda kalır.

Kullanıcı konumlandırma düzenleyicisinin arka planı olarak bir ekran görüntüsü seçerse, NavXS yalnızca bu görüntünün URI'sini ve sistemin verdiği okuma iznini saklar. Görüntü dosyasının kendisi kopyalanmaz, bir uygulama dizinine kaydedilmez, erişilebilirlik hizmetine aktarılmaz ve iletilmez. Arka plan sıfırlandığında kayıtlı URI silinir ve okuma izni yeniden serbest bırakılır.

## 7. İzinler

NavXS, uygulamanın katman işlevselliğini sağlayabilmesi için kullanıcı tarafından açıkça etkinleştirilen erişilebilirlik iznini özellikle gerektirir.

Bunun yanı sıra yalnızca uygulamanın çalışması için teknik olarak gerekli olan erişimler kullanılır.

## 8. Üçüncü Taraflar

Şu an itibarıyla NavXS, yukarıda açıklanan verilerin işlenmesi için üçüncü taraf hizmetleri, analitik araçlar veya reklamcılık kullanmamaktadır.

## 9. Bu Gizlilik Politikasındaki Değişiklikler

Bu gizlilik politikası, uygulamadaki değişiklikleri veya yasal gereklilikleri yansıtmak için gerektiğinde güncellenebilir.

## 10. İletişim

Gizlilik hakkında sorularınız için geliştiriciye ulaşabilirsiniz.

E-posta: navxs@robnice.de
