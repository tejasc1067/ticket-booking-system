package com.ticketbooking.config;

import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.Show;
import com.ticketbooking.entity.User;
import com.ticketbooking.enums.SeatStatus;
import com.ticketbooking.enums.SeatType;
import com.ticketbooking.enums.UserRole;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.ShowRepository;
import com.ticketbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("=== SEEDING DATABASE ===");
        seedUsers();
        seedAllCities();
        log.info("=== SEEDING COMPLETE ===");
    }

    private void seedUsers() {
        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@ticketbook.io")
                .password(passwordEncoder.encode("admin123"))
                .phone("9000000001")
                .role(UserRole.ADMIN)
                .build();

        User customer = User.builder()
                .fullName("Tejas Chumbalkar")
                .email("tejas@test.com")
                .password(passwordEncoder.encode("password123"))
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.saveAll(List.of(admin, customer));
        log.info("Created admin (admin@ticketbook.io / admin123) and customer (tejas@test.com / password123)");
    }

    private void seedAllCities() {
        Map<String, List<String>> cityVenues = new LinkedHashMap<>();
        cityVenues.put("Pandharpur", List.of("Vitthal Mandir Grounds", "Sant Tukaram Natyagruha", "Chandrabhaga Amphitheatre", "Pandharpur Town Hall"));
        cityVenues.put("Mumbai", List.of("NCPA Mumbai", "Prithvi Theatre", "Wankhede Stadium", "Jio World Convention Centre", "Shanmukhananda Hall", "Nehru Centre"));
        cityVenues.put("Pune", List.of("Sawai Gandharva Hall", "Bal Gandharva Rang Mandir", "Tilak Smarak Mandir", "Yashwantrao Chavan Natyagruha", "Ganesh Kala Krida Manch"));
        cityVenues.put("Satara", List.of("Rajwada Grounds", "Ajinkyatara Fort Arena", "Satara Town Hall", "Karad Cultural Centre"));
        cityVenues.put("Sangli", List.of("Vishrambag Wada Grounds", "Sangli Town Hall", "Irwin Bridge Arena", "Miraj Music Academy"));
        cityVenues.put("Kolhapur", List.of("Keshavrao Bhosle Natyagruha", "Shahu Maharaj Hall", "Rankala Lake Arena", "New Palace Grounds", "Kolhapur University Auditorium"));
        cityVenues.put("Nagpur", List.of("Suresh Bhat Sabhagruha", "Reshimbagh Ground", "Deshpande Hall", "Chitnavis Centre", "Kasturchand Park Arena"));
        cityVenues.put("Latur", List.of("Ambedkar Stadium", "Latur Town Hall", "Ausa Road Grounds", "Udgir Cultural Centre"));
        cityVenues.put("Nanded", List.of("Gurudwara Grounds", "Nanded Town Hall", "Vishnupuri Amphitheatre", "Degloor Road Arena"));
        cityVenues.put("Nashik", List.of("Kalidas Kala Mandir", "Nashik Convention Centre", "Sula Vineyards Arena", "Panchavati Grounds", "Mhasoba Mandir Ground"));

        int totalShows = 0;
        for (Map.Entry<String, List<String>> entry : cityVenues.entrySet()) {
            int shows = seedCity(entry.getKey(), entry.getValue());
            totalShows += shows;
            log.info("Seeded {} with {} shows", entry.getKey(), shows);
        }
        log.info("Total events: {}, Total shows: {}", eventRepository.count(), totalShows);
    }

    private int seedCity(String city, List<String> venues) {
        List<EventTemplate> templates = getEventTemplates(city);
        Collections.shuffle(templates);

        int showCount = 0;
        Random random = new Random();
        int targetShows = 50 + random.nextInt(31); // 50-80

        int eventIndex = 0;
        while (showCount < targetShows && eventIndex < templates.size()) {
            EventTemplate t = templates.get(eventIndex);
            String venue = venues.get(random.nextInt(venues.size()));

            Event event = Event.builder()
                    .name(t.name)
                    .description(t.description)
                    .venue(venue)
                    .city(city)
                    .imageUrl(t.imageUrl)
                    .build();

            event = eventRepository.save(event);

            // 2-5 shows per event
            int numShows = Math.min(2 + random.nextInt(4), targetShows - showCount);
            for (int i = 0; i < numShows; i++) {
                LocalDateTime showTime = LocalDateTime.now()
                        .plusDays(1 + random.nextInt(60))
                        .withHour(10 + random.nextInt(12))
                        .withMinute(random.nextBoolean() ? 0 : 30)
                        .withSecond(0)
                        .withNano(0);

                BigDecimal basePrice = BigDecimal.valueOf(200 + random.nextInt(2800));
                int rows = 5 + random.nextInt(11); // 5-15 rows
                int seatsPerRow = 8 + random.nextInt(13); // 8-20 seats per row
                int totalSeats = rows * seatsPerRow;

                Show show = Show.builder()
                        .event(event)
                        .showTime(showTime)
                        .totalSeats(totalSeats)
                        .availableSeats(totalSeats)
                        .basePrice(basePrice)
                        .seats(new ArrayList<>())
                        .build();

                // Generate seats
                List<Seat> seats = generateSeats(show, rows, seatsPerRow, basePrice);
                show.setSeats(seats);
                showRepository.save(show);
                showCount++;
            }
            eventIndex++;
        }
        return showCount;
    }

    private List<Seat> generateSeats(Show show, int rows, int seatsPerRow, BigDecimal basePrice) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            String rowName = String.valueOf((char) ('A' + row));
            SeatType seatType;
            double position = (double) row / rows;
            if (position < 0.2) seatType = SeatType.VIP;
            else if (position < 0.5) seatType = SeatType.PREMIUM;
            else seatType = SeatType.REGULAR;

            BigDecimal price = switch (seatType) {
                case VIP -> basePrice.multiply(BigDecimal.valueOf(2.0));
                case PREMIUM -> basePrice.multiply(BigDecimal.valueOf(1.5));
                case REGULAR -> basePrice;
            };

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                seats.add(Seat.builder()
                        .show(show)
                        .seatNumber(rowName + seatNum)
                        .rowName(rowName)
                        .seatType(seatType)
                        .status(SeatStatus.AVAILABLE)
                        .price(price)
                        .build());
            }
        }
        return seats;
    }

    private List<EventTemplate> getEventTemplates(String city) {
        List<EventTemplate> all = new ArrayList<>();

        // Concerts (20)
        all.add(new EventTemplate("Arijit Singh Live", "Experience the magic of Arijit Singh performing his greatest hits live in concert.", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800"));
        all.add(new EventTemplate("Shreya Ghoshal Musical Night", "A mesmerizing evening of melodious tunes with the nightingale of Bollywood.", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800"));
        all.add(new EventTemplate("AR Rahman Symphony", "Oscar-winning composer AR Rahman in a symphonic concert experience.", "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=800"));
        all.add(new EventTemplate("Vishal-Shekhar Live", "High-energy Bollywood music night with the dynamic duo.", "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800"));
        all.add(new EventTemplate("Nucleya Bass Drop", "India's biggest electronic music artist drops bass like never before.", "https://images.unsplash.com/photo-1574391884720-bbc3740c59d1?w=800"));
        all.add(new EventTemplate("Shankar Mahadevan Unplugged", "Soulful melodies in an intimate acoustic setting.", "https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=800"));
        all.add(new EventTemplate("Sunidhi Chauhan Live", "Powerhouse vocals and electrifying stage presence.", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800"));
        all.add(new EventTemplate("Indian Ocean Roots Tour", "India's iconic rock band performs their legendary tracks.", "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=800"));
        all.add(new EventTemplate("Prateek Kuhad Acoustic Night", "Indie sensation Prateek Kuhad in a soulful acoustic session.", "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800"));
        all.add(new EventTemplate("Amit Trivedi Live", "Film composer extraordinaire performs movie soundtracks live.", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800"));
        all.add(new EventTemplate("Raftaar Hip Hop Night", "The godfather of Indian hip hop ignites the stage.", "https://images.unsplash.com/photo-1547355253-ff0740f6e8c1?w=800"));
        all.add(new EventTemplate("Sonu Nigam Concert", "The golden voice of Bollywood performing timeless classics.", "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800"));
        all.add(new EventTemplate("Lucky Ali Evening", "Sufi rock legend Lucky Ali in an unforgettable evening.", "https://images.unsplash.com/photo-1478147427282-58a87a120781?w=800"));
        all.add(new EventTemplate("Ritviz Electronic Set", "Gen-Z's favourite electronic artist in a vibrant show.", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800"));
        all.add(new EventTemplate("Mohit Chauhan Live", "The voice behind iconic Bollywood tracks performs live.", "https://images.unsplash.com/photo-1499364615650-ec38552f4f34?w=800"));

        // Comedy (10)
        all.add(new EventTemplate("Zakir Khan — Haq Se Single", "India's favourite comedian delivers laughs in his signature style.", "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=800"));
        all.add(new EventTemplate("Vir Das World Tour", "Internationally acclaimed comedian Vir Das live on stage.", "https://images.unsplash.com/photo-1527224857830-43a7acc85260?w=800"));
        all.add(new EventTemplate("Biswa Kalyan Rath StandUp", "Sharp observational humour from one of India's best.", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800"));
        all.add(new EventTemplate("Abhishek Upmanyu — Thoda Aur", "Internet's favourite comedian performing new material.", "https://images.unsplash.com/photo-1527224857830-43a7acc85260?w=800&q=80"));
        all.add(new EventTemplate("Comicstaan Live", "The best comedians from Amazon's Comicstaan together on one stage.", "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=800"));
        all.add(new EventTemplate("Kusha Kapila Comedy Night", "Social media sensation brings her brand of humour to stage.", "https://images.unsplash.com/photo-1496024840928-4c417adf211d?w=800"));
        all.add(new EventTemplate("Anubhav Singh Bassi Live", "The viral sensation performing his hilarious life stories.", "https://images.unsplash.com/photo-1485095329183-d0797cdc5676?w=800"));
        all.add(new EventTemplate("Kenny Sebastian Musical Comedy", "Comedy meets music in this unique performance.", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800"));

        // Theatre & Drama (10)
        all.add(new EventTemplate("Mughal-E-Azam — The Musical", "The epic love story reimagined as a grand stage musical.", "https://images.unsplash.com/photo-1503095396549-807759245b35?w=800"));
        all.add(new EventTemplate("Natsamrat — Marathi Natak", "The legendary Marathi play performed by acclaimed artists.", "https://images.unsplash.com/photo-1507924538820-ede94a04019d?w=800"));
        all.add(new EventTemplate("Moruchi Mavshi — Comedy Natak", "Iconic Marathi comedy that has entertained generations.", "https://images.unsplash.com/photo-1503095396549-807759245b35?w=800&q=80"));
        all.add(new EventTemplate("Vichitra Veer Abhimanyu", "A mythological drama with stunning visual effects.", "https://images.unsplash.com/photo-1460881680858-30d872d5b530?w=800"));
        all.add(new EventTemplate("Hamlet — English Theatre", "Shakespeare's masterpiece performed in contemporary style.", "https://images.unsplash.com/photo-1586899028174-e7098604235b?w=800"));
        all.add(new EventTemplate("Tee Phulrani — Musical Drama", "A beautiful Marathi musical about love and sacrifice.", "https://images.unsplash.com/photo-1565035010268-a3816f98589a?w=800"));
        all.add(new EventTemplate("Zanducha Palna — Folk Play", "Traditional Maharashtrian folk performance.", "https://images.unsplash.com/photo-1583795128727-6ec3642408f8?w=800"));

        // Dance & Cultural (8)
        all.add(new EventTemplate("Kathak Mahotsav", "Classical Kathak dance festival featuring national performers.", "https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=800"));
        all.add(new EventTemplate("Lavani Night", "Traditional Maharashtrian Lavani dance performances.", "https://images.unsplash.com/photo-1504609813442-a8924e83f76e?w=800"));
        all.add(new EventTemplate("Bharatanatyam Evening", "South Indian classical dance recital by renowned dancers.", "https://images.unsplash.com/photo-1518834107812-67b0b7c58434?w=800"));
        all.add(new EventTemplate("Bollywood Dance Night", "High-energy Bollywood dance show with celebrity choreographers.", "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800"));
        all.add(new EventTemplate("Garba Raas Festival", "Multi-day Gujarati folk dance celebration.", "https://images.unsplash.com/photo-1528495612343-9ca9f4a4de28?w=800"));
        all.add(new EventTemplate("Contemporary Dance Showcase", "Modern dance pieces exploring social themes.", "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800&q=80"));

        // Sports & Adventure (5)
        all.add(new EventTemplate("Pro Kabaddi Live", "Watch India's top kabaddi teams battle it out live.", "https://images.unsplash.com/photo-1517164850305-99a3e65bb47e?w=800&q=80"));
        all.add(new EventTemplate("Marathon Championship", "Annual city marathon with professional runners.", "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?w=800"));
        all.add(new EventTemplate("Cricket Exhibition Match", "Celebrity cricket match featuring Bollywood and cricket stars.", "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800"));
        all.add(new EventTemplate("Kushti Wrestling Dangal", "Traditional Indian wrestling tournament.", "https://images.unsplash.com/photo-1517164850305-99a3e65bb47e?w=800"));

        // Festivals & Fairs (8)
        all.add(new EventTemplate("Ganeshotsav Cultural Fest", "Week-long cultural celebration with music, dance and drama.", "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=800&q=80"));
        all.add(new EventTemplate("Diwali Mela — Light Festival", "Grand Diwali celebration with performances and fireworks.", "https://images.unsplash.com/photo-1576089172869-4f5f6f315620?w=800&q=80"));
        all.add(new EventTemplate("Food & Music Festival", "A fusion of live music and Maharashtra's best cuisine.", "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800"));
        all.add(new EventTemplate("Holi Rang Utsav", "Colours, music and dance in a grand Holi celebration.", "https://images.unsplash.com/photo-1576089172869-4f5f6f315620?w=800"));
        all.add(new EventTemplate("Navratri Garba Night", "Nine nights of devotion, music and Garba dancing.", "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=800"));

        // Workshops & Talks (5)
        all.add(new EventTemplate("TEDx Talks Live", "Inspiring talks from thought leaders and innovators.", "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800"));
        all.add(new EventTemplate("Startup Summit 2026", "India's biggest startup conference and networking event.", "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800"));
        all.add(new EventTemplate("Photography Masterclass", "Learn professional photography from award-winning photographers.", "https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=800"));
        all.add(new EventTemplate("Screenwriting Workshop", "Craft compelling screenplays with industry professionals.", "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800"));
        all.add(new EventTemplate("AI & Tech Conference", "Explore the future of technology and artificial intelligence.", "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800"));

        // Kids & Family (5)
        all.add(new EventTemplate("Disney Magic Show", "A magical experience for the whole family.", "https://images.unsplash.com/photo-1520342868574-5fa3804e551c?w=800"));
        all.add(new EventTemplate("Puppet Theatre — Ramayana", "Traditional puppet show retelling the epic Ramayana.", "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=800"));
        all.add(new EventTemplate("Kids Science Exhibition", "Interactive science experiments and demonstrations.", "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800"));
        all.add(new EventTemplate("Circus Spectacular", "Acrobats, clowns and jaw-dropping performances.", "https://images.unsplash.com/photo-1509909756405-be0199881695?w=800"));

        // City-specific suffix to avoid name clashes
        String suffix = " — " + city;
        all.forEach(t -> t.name = t.name + suffix);

        return all;
    }

    private static class EventTemplate {
        String name;
        String description;
        String imageUrl;

        EventTemplate(String name, String description, String imageUrl) {
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
        }
    }
}
