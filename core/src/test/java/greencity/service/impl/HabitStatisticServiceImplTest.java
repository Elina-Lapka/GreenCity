package greencity.service.impl;

import greencity.dto.habitstatistic.*;
import greencity.dto.user.HabitLogItemDto;
import greencity.entity.Habit;
import greencity.entity.HabitDictionary;
import greencity.entity.HabitStatistic;
import greencity.entity.User;
import greencity.entity.enums.HabitRate;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.mapping.HabitStatisticMapper;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import greencity.converters.DateService;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

public class HabitStatisticServiceImplTest {

    @Mock
    private HabitStatisticRepo habitStatisticRepo;

    @Mock
    HabitStatisticMapper habitStatisticMapper;

    @Mock
    HabitRepo habitRepo;

    @Mock
    ModelMapper modelMapper;

    @Mock
    DateService dateService;

    private HabitStatisticServiceImpl habitStatisticService;

    private ZonedDateTime zonedDateTime = ZonedDateTime.now();

    private AddHabitStatisticDto addhs = AddHabitStatisticDto
            .builder().amountOfItems(10).habitRate(HabitRate.GOOD)
            .id(1L).habitId(1L).createdOn(ZonedDateTime.now()).build();

    private Habit habit = new Habit(1L,new HabitDictionary(), new User(), true,
            zonedDateTime, Collections.emptyList());

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        habitStatisticService = new HabitStatisticServiceImpl(habitStatisticRepo, habitRepo,
            habitStatisticMapper, modelMapper, dateService);
    }

    private HabitStatistic habitStatistic = new HabitStatistic(
        1L, HabitRate.GOOD, ZonedDateTime.now(), 10, null);

    @Test
    public void saveTest() {
        when(dateService.convertToDatasourceTimezone(any())).thenReturn(ZonedDateTime.now());
        when(habitStatisticService.save(addhs)).thenReturn(addhs);
        assertEquals(addhs, habitStatisticService.save(addhs));
    }

    @Test
    public void saveBeforeDayTest() {
        when(dateService.convertToDatasourceTimezone(any())).thenReturn(ZonedDateTime.now().minusDays(1));
        when(habitStatisticService.save(addhs)).thenReturn(addhs);
        assertEquals(addhs, habitStatisticService.save(addhs));
    }

    @Test(expected = NotSavedException.class)
    public void saveExceptionTest(){
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
                addhs.getHabitId())).thenReturn(Optional.of(new HabitStatistic()));
        habitStatisticService.save(addhs);
    }

    @Test(expected = BadRequestException.class)
    public void saveExceptionBadRequestTest(){
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
                addhs.getHabitId())).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreatedOn())).thenReturn(ZonedDateTime.now().plusDays(2));
        when(habitStatisticMapper.convertToEntity(addhs)).thenReturn(new HabitStatistic());
        when(habitStatisticMapper.convertToDto(new HabitStatistic())).thenReturn(addhs);
        when(habitStatisticRepo.save(new HabitStatistic())).thenReturn(new HabitStatistic());
        habitStatisticService.save(addhs);
    }

    @Test(expected = BadRequestException.class)
    public void saveExceptionBadRequestMinusDayTest(){
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
                addhs.getHabitId())).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreatedOn())).thenReturn(ZonedDateTime.now().minusDays(2));
        when(habitStatisticMapper.convertToEntity(addhs)).thenReturn(new HabitStatistic());
        when(habitStatisticMapper.convertToDto(new HabitStatistic())).thenReturn(addhs);
        when(habitStatisticRepo.save(new HabitStatistic())).thenReturn(new HabitStatistic());
        habitStatisticService.save(addhs);
    }

    @Test
    public void updateTest() {
        UpdateHabitStatisticDto updateHabStatDto = new UpdateHabitStatisticDto();
        HabitStatistic habitStatistic = new HabitStatistic();
        when(habitStatisticRepo.findById(anyLong())).thenReturn(Optional.of(habitStatistic));
        when(habitStatisticService.update(anyLong(), updateHabStatDto)).thenReturn(updateHabStatDto);

        assertEquals(updateHabStatDto, habitStatisticService.update(anyLong(), updateHabStatDto));
        Mockito.verify(habitStatisticRepo, times(2)).findById(anyLong());
    }

    @Test
    public void findByIdTest() {
        HabitStatistic habitStatistic = new HabitStatistic();
        when(habitStatisticRepo.findById(anyLong())).thenReturn(Optional.of(habitStatistic));
        assertEquals(habitStatistic, habitStatisticService.findById(anyLong()));
    }

    @Test(expected = NotFoundException.class)
    public void findByIdExceptionTest() {
        when(habitStatisticRepo.findById(anyLong())).thenReturn(Optional.empty());
        habitStatisticService.findById(anyLong());
    }

    @Test
    public void findAllHabitsByUserIdTest() {
        List<Habit> habits = Arrays.asList(new Habit(), new Habit());
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(habits));
        assertEquals(habits, habitStatisticService.findAllHabitsByUserId(anyLong()));
    }

    @Test(expected = NotFoundException.class)
    public void findAllHabitsByUserIdExceptionTest() {
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.empty());
        habitStatisticService.findAllHabitsByUserId(anyLong());
    }

    @Test
    public void findAllByHabitIdTest() {
        List<HabitStatistic> habitStatistic = new ArrayList<>();
        when(habitStatisticRepo.findAllByHabitId(anyLong())).thenReturn(habitStatistic);

        List<HabitStatisticDto> habitStatisticDtos =
            habitStatistic
                .stream()
                .map(source -> modelMapper.map(source, HabitStatisticDto.class))
                .collect(Collectors.toList());

        assertEquals(habitStatisticService.findAllByHabitId(anyLong()), habitStatisticDtos);
    }

    @Test
    public void getTodayStatisticsForAllHabitItemsTest() {
        when(habitStatisticRepo.getStatisticsForAllHabitItemsByDate(new Date(), "en"))
                .thenReturn(new ArrayList<>());
        assertEquals(new ArrayList<HabitItemsAmountStatisticDto>(),
                habitStatisticService.getTodayStatisticsForAllHabitItems("en"));
    }

    @Test(expected = NotFoundException.class)
    public void findAllHabitsByStatusExceptionTest() {
        when(habitStatisticService.findAllHabitsByUserId(anyLong())).thenReturn(Collections.emptyList());
        habitStatisticService.findAllHabitsByStatus(1L, false);
    }

    @Test
    public void findAllHabitsByStatusTest() {
        List<Habit> list = Collections.singletonList(habit);
        when(habitRepo.findAllByUserId(1L)).thenReturn(Optional.of(list));
        assertEquals(list, habitStatisticService.findAllHabitsByStatus(1L, true));
    }

    @Test(expected = NotFoundException.class)
    public void getInfoAboutUserHabitsExceptionTest() {
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.emptyList()));
        habitStatisticService.getInfoAboutUserHabits(1L);
    }

    @Test
    public void getInfoAboutUserHabitsTest() {
        CalendarUsefulHabitsDto calendarUsefulHabitsDto = new CalendarUsefulHabitsDto();
        calendarUsefulHabitsDto.setAllItemsPerMonth(Collections.singletonList(
                new HabitLogItemDto(null,0)));
        calendarUsefulHabitsDto.setCreationDate(zonedDateTime);
        calendarUsefulHabitsDto.setDifferenceUnTakenItemsWithPreviousDay(
                Collections.singletonList(new HabitLogItemDto(null, 0)));
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.singletonList(habit)));
        when(dateService.getDatasourceZonedDateTime()).thenReturn(ZonedDateTime.now());
        when(habitStatisticRepo.getSumOfAllItemsPerMonth(1L, ZonedDateTime.now()))
                .thenReturn(Optional.of(0));
        assertEquals(calendarUsefulHabitsDto, habitStatisticService.getInfoAboutUserHabits(anyLong()));
    }

    @Test
    public void getInfoAboutUserHabitsOneTest() {
        CalendarUsefulHabitsDto calendarUsefulHabitsDto = new CalendarUsefulHabitsDto();
        calendarUsefulHabitsDto.setAllItemsPerMonth(Collections.singletonList(
                new HabitLogItemDto(null,0)));
        calendarUsefulHabitsDto.setCreationDate(zonedDateTime);
        calendarUsefulHabitsDto.setDifferenceUnTakenItemsWithPreviousDay(
                Collections.singletonList(new HabitLogItemDto(null, 0)));
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.singletonList(habit)));
        when(dateService.getDatasourceZonedDateTime()).thenReturn(zonedDateTime);
        when(habitStatisticRepo.getSumOfAllItemsPerMonth(1L, zonedDateTime))
                .thenReturn(Optional.of(12));
        assertEquals(calendarUsefulHabitsDto, habitStatisticService.getInfoAboutUserHabits(anyLong()));
    }

    @Test(expected = NotFoundException.class)
    public void findAllHabitsAndTheirStatisticsExceptionTest() {
        when(habitRepo.findAllByUserId(1L)).thenReturn(Optional.of(Collections.emptyList()));
        habitStatisticService.findAllHabitsAndTheirStatistics(1L, true,"en");
    }

}
